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
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonBuilderAnnotations
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonDeserializeAnnotations
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonIgnorePropertiesAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonPropertyAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.jvmNameAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.sanitizeKdoc
import com.netflix.graphql.dgs.codegen.generators.kotlin.sanitizeKotlinIdentifier
import com.netflix.graphql.dgs.codegen.generators.kotlin.suppressInapplicableJvmNameAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.toKtTypeName
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findTypeExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.joinToCode
import graphql.language.Document
import graphql.language.FieldDefinition
import graphql.language.ObjectTypeDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.util.Objects

internal val logger: Logger = LoggerFactory.getLogger("com.netflix.graphql.dgs.codegen.generators.kotlin2")

private data class KotlinFieldInfo(
    val definition: FieldDefinition,
    val kotlinName: String = sanitizeKotlinIdentifier(definition.name),
) {
    val graphQLName: String = definition.name
}

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
            val fieldDefinitions =
                sequenceOf(typeDefinition)
                    .plus(extensionTypes)
                    .flatMap { it.fieldDefinitions }
                    .filterSkipped()
                    .filter(ReservedKeywordFilter.filterInvalidNames)
                    .toList()

            val fields = fieldDefinitions.map(::KotlinFieldInfo)

            fun type(field: KotlinFieldInfo) = typeLookup.findReturnType(config.packageNameTypes, field.definition.type)

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
                                    name = "${field.kotlinName}Default",
                                    type = LambdaTypeName.get(returnType = type(field)),
                                ).addModifiers(KModifier.PRIVATE)
                                .initializer(
                                    buildCodeBlock {
                                        addStatement(
                                            "\n{ throw %T(%S) }",
                                            IllegalStateException::class,
                                            "Field `${field.graphQLName}` was not requested",
                                        )
                                    },
                                ).build()
                        },
                    ).build()

            val className = ClassName(config.packageNameTypes, typeDefinition.name)

            // create a builder for this class; default to lambda that throws if accessed
            val builderClassName = ClassName(config.packageNameTypes, typeDefinition.name, "Builder")
            val builder =
                TypeSpec
                    .classBuilder("Builder")
                    .addOptionalGeneratedAnnotation(config)
                    .apply { jsonBuilderAnnotations(config).forEach { addAnnotation(it) } }
                    .addAnnotation(jsonIgnorePropertiesAnnotation("__typename"))
                    // add a backing property for each field
                    .addProperties(
                        fields.map { field ->
                            PropertySpec
                                .builder(
                                    name = field.kotlinName,
                                    type = LambdaTypeName.get(returnType = type(field)),
                                ).addModifiers(KModifier.PRIVATE)
                                .mutable()
                                .initializer("${field.kotlinName}Default")
                                .build()
                        },
                    )
                    // add a method to set the field
                    .addFunctions(
                        fields.map { field ->
                            FunSpec
                                .builder("with${field.kotlinName.capitalized()}")
                                .addAnnotation(jsonPropertyAnnotation(field.graphQLName))
                                .addParameter(field.kotlinName, type(field))
                                .addControlFlow("return this.apply") {
                                    addStatement("this.%N = { %N }", field.kotlinName, field.kotlinName)
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
                                    fs.forEach { f -> builder.add("  %N = %N,\n", f.kotlinName, f.kotlinName) }
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
                    .apply { jsonDeserializeAnnotations(config, builderClassName).forEach { addAnnotation(it) } }
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
                                            name = field.kotlinName,
                                            type = LambdaTypeName.get(returnType = type(field)),
                                        ).defaultValue("${field.kotlinName}Default")
                                        .build()
                                },
                            ).build(),
                    )
                    // add a backing property for each field
                    .addProperties(
                        fields.map { field ->
                            PropertySpec
                                .builder(
                                    name = "__${field.kotlinName}",
                                    type = LambdaTypeName.get(returnType = type(field)),
                                ).addModifiers(KModifier.PRIVATE)
                                .initializer("%N", field.kotlinName)
                                .build()
                        },
                    )
                    // add a getter for each field
                    .addProperties(
                        fields.map { field ->
                            PropertySpec
                                .builder(
                                    name = field.kotlinName,
                                    type = type(field),
                                ).apply {
                                    if (field.definition.description != null) {
                                        addKdoc("%L", field.definition.description.sanitizeKdoc())
                                    }
                                }.apply {
                                    if (field.graphQLName in overrideFields) {
                                        addModifiers(KModifier.OVERRIDE)
                                        addAnnotation(suppressInapplicableJvmNameAnnotation())
                                    }
                                }.addAnnotation(jvmNameAnnotation(field.kotlinName))
                                .getter(
                                    FunSpec
                                        .getterBuilder()
                                        .addStatement("return __${field.kotlinName}.invoke()")
                                        .build(),
                                ).build()
                        },
                    )
                    // add value-based equals/hashCode over the requested fields
                    .addFunctions(equalsAndHashCodeFunctions(className, fields))
                    .build()

            // return a file per type
            FileSpec.get(config.packageNameTypes, typeSpec)
        }
}

/**
 * Generates `equals`/`hashCode` for a kotlin2 data type.
 *
 * These classes have no fixed value per field: a field's backing lambda is either the
 * one supplied by the caller (the field was "requested") or the shared `${name}Default`
 * lambda from the companion object, which throws when invoked (the field was not
 * requested). Two instances are equal iff they requested the exact same set of fields
 * and every requested field's value is equal; a field left unrequested on both sides is
 * never invoked and counts as equal, while a field requested on only one side makes the
 * instances unequal. This keeps the relation symmetric and transitive - comparing only
 * the intersection of requested fields would not.
 *
 * Whether a field was requested is detected by reference-comparing its backing lambda
 * against the companion's default lambda, never by invoking it or matching the
 * exception message.
 */
private class EqualsHashCodeFieldNames(
    field: KotlinFieldInfo,
) {
    // Computed as whole identifiers (not "__" + %N) so KotlinPoet's keyword-escaping of a single
    // %N argument (e.g. `interface` -> `` `interface` ``) can't land in the middle of a token
    // like "__`interface`", which isn't valid Kotlin.
    val backing = "__${field.kotlinName}"
    val default = "${field.kotlinName}Default"
    val getter = field.kotlinName
}

private fun equalsAndHashCodeFunctions(
    className: ClassName,
    fields: List<KotlinFieldInfo>,
): List<FunSpec> {
    if (fields.isEmpty()) {
        return emptyList()
    }

    val names = fields.map(::EqualsHashCodeFieldNames)

    val equalsFun =
        FunSpec
            .builder("equals")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("other", ANY.copy(nullable = true))
            .returns(BOOLEAN)
            .addStatement("if (this === other) return true")
            .addStatement("if (other !is %T) return false", className)
            .addCode(
                buildCodeBlock {
                    add("return ")
                    add(
                        names
                            .map { n ->
                                CodeBlock.of(
                                    "(%N === %N) == (other.%N === %N) && (%N === %N || %T.equals(%N, other.%N))",
                                    n.backing,
                                    n.default,
                                    n.backing,
                                    n.default,
                                    n.backing,
                                    n.default,
                                    Objects::class,
                                    n.getter,
                                    n.getter,
                                )
                            }.joinToCode(separator = " &&\n"),
                    )
                    add("\n")
                },
            ).build()

    val hashCodeFun =
        FunSpec
            .builder("hashCode")
            .addModifiers(KModifier.OVERRIDE)
            .returns(INT)
            .addStatement(
                "return %T.hash(%L)",
                Objects::class,
                names
                    .map { n ->
                        // Feed the shared default lambda itself as the "unrequested" sentinel: it's
                        // already a stable per-field singleton, so it doubles as a hashCode marker
                        // without needing a dedicated sentinel type, and never invoking the supplier.
                        CodeBlock.of(
                            "if (%N === %N) %N else %N",
                            n.backing,
                            n.default,
                            n.default,
                            n.getter,
                        )
                    }.joinToCode(separator = ",\n"),
            ).build()

    return listOf(equalsFun, hashCodeFun)
}
