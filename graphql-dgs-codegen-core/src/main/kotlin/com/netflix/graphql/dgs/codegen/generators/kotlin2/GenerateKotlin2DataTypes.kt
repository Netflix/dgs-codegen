/*
 *
 *  Copyright 2020 Netflix, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.netflix.graphql.dgs.codegen.generators.kotlin2

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.filterSkipped
import com.netflix.graphql.dgs.codegen.generators.kotlin.ReservedKeywordFilter
import com.netflix.graphql.dgs.codegen.generators.kotlin.addControlFlow
import com.netflix.graphql.dgs.codegen.generators.kotlin.addOptionalGeneratedAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.disableJsonTypeInfoAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonBuilderAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonDeserializeAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonIgnorePropertiesAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonPropertyAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.jvmNameAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.sanitizeKdoc
import com.netflix.graphql.dgs.codegen.generators.kotlin.suppressInapplicableJvmNameAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.toKtTypeName
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findTypeExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import graphql.language.Document
import graphql.language.FieldDefinition
import graphql.language.ObjectTypeDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Serializable

internal val logger: Logger = LoggerFactory.getLogger("com.netflix.graphql.dgs.codegen.generators.kotlin2")

fun generateKotlin2DataTypes(
    config: CodeGenConfig,
    document: Document,
    requiredTypes: Set<String>,
): List<FileSpec> {
    val typeLookup = Kotlin2TypeLookup(config, document)

    return document
        .getDefinitionsOfType(ObjectTypeDefinition::class.java)
        .excludeSchemaTypeExtension()
        .filter { config.generateDataTypes || it.name in requiredTypes }
        .filter { !it.shouldSkip(config) }
        .map { typeDefinition ->

            logger.info("Generating data type {}", typeDefinition.name)
            // get all interfaces this type implements
            val implementedInterfaces = typeLookup.implementedInterfaces(typeDefinition)
            val implementedUnionTypes = typeLookup.implementedUnionTypes(typeDefinition.name)
            val superInterfaces = implementedInterfaces + implementedUnionTypes

            // get any fields defined via schema extensions
            val extensionTypes = findTypeExtensions(typeDefinition.name, document.definitions)

            // get all fields defined on the type itself or any extension types
            val fields =
                sequenceOf(typeDefinition)
                    .plus(extensionTypes)
                    .flatMap { it.fieldDefinitions }
                    .filterSkipped()
                    .filter(ReservedKeywordFilter.filterInvalidNames)
                    .toList()

            fun type(field: FieldDefinition) = typeLookup.findReturnType(config.packageNameTypes, field.type)

            // get a list of fields to override
            val overrideFields = typeLookup.overrideFields(implementedInterfaces)

            // create a companion object to store defaults for each field
            val companionObject =
                TypeSpec
                    .companionObjectBuilder()
                    .addOptionalGeneratedAnnotation(config)
                    // add a default lambda for each field that throws if accessed
                    .addProperties(
                        fields.map { field ->
                            PropertySpec
                                .builder(
                                    name = "${field.name}Default",
                                    type = LambdaTypeName.get(returnType = type(field)),
                                ).addModifiers(KModifier.PRIVATE)
                                .initializer(
                                    buildCodeBlock {
                                        addStatement(
                                            "\n{ throw %T(%S) }",
                                            IllegalStateException::class,
                                            "Field `${field.name}` was not requested",
                                        )
                                    },
                                ).build()
                        },
                    ).build()

            // create a builder for this class; default to lambda that throws if accessed
            val builderClassName = ClassName(config.packageNameTypes, typeDefinition.name, "Builder")
            val builder =
                TypeSpec
                    .classBuilder("Builder")
                    .addOptionalGeneratedAnnotation(config)
                    .addAnnotation(jsonBuilderAnnotation())
                    .addAnnotation(jsonIgnorePropertiesAnnotation("__typename"))
                    // add a backing property for each field
                    .addProperties(
                        fields.map { field ->
                            PropertySpec
                                .builder(
                                    name = field.name,
                                    type = LambdaTypeName.get(returnType = type(field)),
                                ).addModifiers(KModifier.PRIVATE)
                                .mutable()
                                .initializer("${field.name}Default")
                                .build()
                        },
                    )
                    // add a method to set the field
                    .addFunctions(
                        fields.map { field ->
                            FunSpec
                                .builder("with${field.name.capitalized()}")
                                .addAnnotation(jsonPropertyAnnotation(field.name))
                                .addParameter(field.name, type(field))
                                .addControlFlow("return this.apply") {
                                    addStatement("this.%N = { %N }", field.name, field.name)
                                }.returns(builderClassName)
                                .build()
                        },
                    )
                    // add a build method to return the constructed class
                    .addFunction(
                        FunSpec
                            .builder("build")
                            .returns(typeDefinition.name.toKtTypeName())
                            .addCode(
                                fields.let { fs ->
                                    val builder =
                                        CodeBlock.builder().add(
                                            "return %T(\n",
                                            ClassName(config.packageNameTypes, typeDefinition.name),
                                        )
                                    fs.forEach { f -> builder.add("  %N = %N,\n", f.name, f.name) }
                                    builder.add(")").build()
                                },
                            ).build(),
                    ).build()

            // create the data class
            val typeSpec =
                TypeSpec
                    .classBuilder(typeDefinition.name)
                    .addOptionalGeneratedAnnotation(config)
                    // add docs if available
                    .apply {
                        if (typeDefinition.description != null) {
                            addKdoc("%L", typeDefinition.description.sanitizeKdoc())
                        }
                    }
                    // add jackson annotations
                    .addAnnotation(disableJsonTypeInfoAnnotation())
                    .addAnnotation(
                        jsonDeserializeAnnotation(
                            builderClassName,
                        ),
                    )
                    // add nested classes
                    .addType(companionObject)
                    .addType(builder)
                    // add interfaces to implement
                    .addSuperinterfaces(
                        superInterfaces.map { typeLookup.findKtInterfaceName(it, config.packageNameTypes) },
                    )
                    // add Serializable interface if requested
                    .apply {
                        if (config.implementSerializable) {
                            addSuperinterface(Serializable::class.asClassName())
                        }
                    }
                    // add a constructor with a supplier for every field
                    .primaryConstructor(
                        FunSpec
                            .constructorBuilder()
                            .addParameters(
                                fields.map { field ->
                                    ParameterSpec
                                        .builder(
                                            name = field.name,
                                            type = LambdaTypeName.get(returnType = type(field)),
                                        ).defaultValue("${field.name}Default")
                                        .build()
                                },
                            ).build(),
                    )
                    // add a backing property for each field
                    .addProperties(
                        fields.map { field ->
                            PropertySpec
                                .builder(
                                    name = "__${field.name}",
                                    type = LambdaTypeName.get(returnType = type(field)),
                                ).addModifiers(KModifier.PRIVATE)
                                .initializer("%N", field.name)
                                .build()
                        },
                    )
                    // add a getter for each field
                    .addProperties(
                        fields.map { field ->
                            PropertySpec
                                .builder(
                                    name = field.name,
                                    type = type(field),
                                ).apply {
                                    if (field.description != null) {
                                        addKdoc("%L", field.description.sanitizeKdoc())
                                    }
                                }.apply {
                                    if (field.name in overrideFields) {
                                        addModifiers(KModifier.OVERRIDE)
                                        addAnnotation(suppressInapplicableJvmNameAnnotation())
                                    }
                                }.addAnnotation(jvmNameAnnotation(field.name))
                                .getter(
                                    FunSpec
                                        .getterBuilder()
                                        .addStatement("return __${field.name}.invoke()")
                                        .build(),
                                ).build()
                        },
                    ).build()

            // return a file per type
            FileSpec.get(config.packageNameTypes, typeSpec)
        }
}
