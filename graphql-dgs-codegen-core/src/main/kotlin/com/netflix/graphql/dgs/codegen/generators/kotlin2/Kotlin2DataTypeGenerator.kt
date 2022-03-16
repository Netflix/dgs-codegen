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
import com.netflix.graphql.dgs.codegen.generators.kotlin.KotlinTypeUtils
import com.netflix.graphql.dgs.codegen.generators.kotlin.ReservedKeywordFilter
import com.netflix.graphql.dgs.codegen.generators.kotlin.addControlFlow
import com.netflix.graphql.dgs.codegen.generators.kotlin.disableJsonTypeInfoAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonBuilderAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonDeserializeAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonIgnorePropertiesAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonPropertyAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.sanitizeKdoc
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import graphql.language.Description
import graphql.language.Document
import graphql.language.InterfaceTypeDefinition
import graphql.language.NamedNode
import graphql.language.ObjectTypeDefinition
import graphql.language.UnionTypeDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.squareup.kotlinpoet.TypeName as KtTypeName

class Kotlin2DataTypeGenerator {

    private data class Field(
        val name: String,
        val type: KtTypeName,
        val description: Description?,
    )

    companion object {

        private val logger: Logger = LoggerFactory.getLogger(Kotlin2DataTypeGenerator::class.java)

        fun generate(
            config: CodeGenConfig,
            document: Document,
            requiredTypes: Set<String>,
        ): List<FileSpec> {

            val typeUtils = KotlinTypeUtils(config.packageNameTypes, config)

            // get a map of interfaces > fields
            val interfaceFields = document
                .getDefinitionsOfType(InterfaceTypeDefinition::class.java)
                .associate { i -> i.name to i.fieldDefinitions.map { it.name } }

            // invert the union mapping to create a lookup
            val unionTypes = document
                .getDefinitionsOfType(UnionTypeDefinition::class.java)
                .flatMap { u -> u.memberTypes.filterIsInstance<NamedNode<*>>().map { m -> m.name to u.name } }
                .groupBy(keySelector = { p -> p.first }, valueTransform = { p -> p.second })

            return document
                .getDefinitionsOfType(ObjectTypeDefinition::class.java)
                .excludeSchemaTypeExtension()
                .filter { config.generateDataTypes || it.name in requiredTypes }
                .filter { !it.shouldSkip(config) }
                .map { typeDefinition ->

                    logger.info("Generating data type ${typeDefinition.name}")

                    // get all interfaces this type will implement
                    val interfaces = typeDefinition.implements.filterIsInstance<NamedNode<*>>()
                    val implementedInterfaces = interfaces.map { it.name }
                    val implementedUnionTypes = unionTypes[typeDefinition.name] ?: emptyList()
                    val superInterfaces = implementedInterfaces + implementedUnionTypes

                    // get any fields defined via schema extensions
                    val extensionTypes =
                        SchemaExtensionsUtils.findTypeExtensions(typeDefinition.name, document.definitions)

                    // get all fields defined on the type itself or any extension types
                    val fields = listOf(typeDefinition)
                        .plus(extensionTypes)
                        .flatMap { d -> d.fieldDefinitions }
                        .filterSkipped()
                        .filter(ReservedKeywordFilter.filterInvalidNames)
                        .map {
                            Field(
                                name = it.name,
                                type = typeUtils.findReturnType(it.type),
                                description = it.description,
                            )
                        }

                    // get a list of fields to override
                    val overrideFields = interfaces
                        .mapNotNull { interfaceFields[it.name] }
                        .flatten()
                        .toSet()

                    // create a companion object to store defaults for each field
                    val companionObject = TypeSpec.companionObjectBuilder()
                        // add a default lambda for each field that throws if accessed
                        .addProperties(
                            fields.map { field ->
                                PropertySpec.builder(
                                    "${field.name}Default",
                                    LambdaTypeName.get(returnType = field.type)
                                )
                                    .addModifiers(KModifier.PRIVATE)
                                    .initializer(
                                        buildCodeBlock {
                                            addStatement(
                                                "\n{ throw %T(%S) }",
                                                IllegalStateException::class,
                                                "Field `${field.name}` was not requested"
                                            )
                                        }
                                    )
                                    .build()
                            }
                        )
                        .build()

                    // create a builder for this class; default to lambda that throws if accessed
                    val builderClassName = ClassName(config.packageNameTypes, typeDefinition.name, "Builder")
                    val builder = TypeSpec.classBuilder("Builder")
                        .addAnnotation(jsonBuilderAnnotation())
                        .addAnnotation(jsonIgnorePropertiesAnnotation("__typename"))
                        // add a backing property for each field
                        .addProperties(
                            fields.map { field ->
                                PropertySpec.builder(field.name, LambdaTypeName.get(returnType = field.type))
                                    .addModifiers(KModifier.PRIVATE)
                                    .mutable()
                                    .initializer("${field.name}Default")
                                    .build()
                            }
                        )
                        // add a method to set the field
                        .addFunctions(
                            fields.map { field ->
                                FunSpec.builder("with${field.name.capitalized()}")
                                    .addAnnotation(jsonPropertyAnnotation(field.name))
                                    .addParameter(field.name, field.type)
                                    .addControlFlow("return this.apply") {
                                        addStatement("this.${field.name} = { ${field.name} }")
                                    }
                                    .returns(builderClassName)
                                    .build()
                            }
                        )
                        // add a build method to return the constructed class
                        .addFunction(
                            FunSpec.builder("build")
                                .addStatement("return ${typeDefinition.name}(\n${fields.joinToString("\n") { "  ${it.name} = ${it.name}," }}\n)")
                                .build()
                        )
                        .build()

                    // create the data class
                    val type = TypeSpec.classBuilder(typeDefinition.name)
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
                                builderClassName
                            )
                        )
                        // add nested classes
                        .addType(companionObject)
                        .addType(builder)
                        .addSuperinterfaces(
                            superInterfaces.map { ClassName.bestGuess("${config.packageNameTypes}.$it") }
                        )
                        // add a constructor with a supplier for every field
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameters(
                                    fields.map { field ->
                                        ParameterSpec.builder(
                                            field.name,
                                            LambdaTypeName.get(returnType = field.type)
                                        )
                                            .defaultValue("${field.name}Default")
                                            .build()
                                    }
                                )
                                .build()
                        )
                        // add a backing property for each field
                        .addProperties(
                            fields.map { field ->
                                PropertySpec.builder(
                                    "_${field.name}",
                                    LambdaTypeName.get(returnType = field.type)
                                )
                                    .addModifiers(KModifier.PRIVATE)
                                    .initializer(field.name)
                                    .build()
                            }
                        )
                        // add a getter for each field
                        .addProperties(
                            fields.map { field ->
                                PropertySpec.builder(field.name, field.type)
                                    .apply {
                                        if (field.description != null) {
                                            addKdoc("%L", field.description.sanitizeKdoc())
                                        }
                                    }
                                    .apply {
                                        if (field.name in overrideFields) {
                                            addModifiers(KModifier.OVERRIDE)
                                        }
                                    }
                                    .getter(
                                        FunSpec.getterBuilder()
                                            .addStatement("return _${field.name}.invoke()")
                                            .build()
                                    )
                                    .build()
                            }
                        )
                        .build()

                    // return a file per type
                    FileSpec
                        .builder(config.packageNameTypes, type.name!!)
                        .addType(type)
                        .build()
                }
        }
    }
}
