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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.GraphQLInput
import com.netflix.graphql.dgs.codegen.generators.kotlin.KotlinTypeUtils
import com.netflix.graphql.dgs.codegen.generators.kotlin.ReservedKeywordFilter
import com.netflix.graphql.dgs.codegen.generators.kotlin.addOptionalGeneratedAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.sanitizeKdoc
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInputExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import graphql.language.*

fun generateKotlin2InputTypes(
    config: CodeGenConfig,
    document: Document,
    requiredTypes: Set<String>
): List<FileSpec> {
    val typeLookup = Kotlin2TypeLookup(config, document)

    val typeUtils = KotlinTypeUtils(
        packageName = config.packageName,
        config = config,
        document = document
    )

    return document
        .getDefinitionsOfType(InputObjectTypeDefinition::class.java)
        .excludeSchemaTypeExtension()
        .filter { config.generateDataTypes || it.name in requiredTypes }
        .filter { !it.shouldSkip(config) }
        .map { inputDefinition ->

            logger.info("Generating input type ${inputDefinition.name}")

            // get any fields defined via schema extensions
            val extensionTypes = findInputExtensions(inputDefinition.name, document.definitions)

            // get all fields defined on the type itself or any extension types
            val fields = listOf(inputDefinition)
                .plus(extensionTypes)
                .flatMap { it.inputValueDefinitions }
                .filter(ReservedKeywordFilter.filterInvalidNames)

            fun type(field: InputValueDefinition) = typeLookup.findReturnType(config.packageNameTypes, field.type)

            val typeName = ClassName(config.packageNameTypes, inputDefinition.name)

            // create the input class
            val typeSpec = TypeSpec.classBuilder(typeName)
                .addOptionalGeneratedAnnotation(config)
                // add docs if available
                .apply {
                    if (inputDefinition.description != null) {
                        addKdoc("%L", inputDefinition.description.sanitizeKdoc())
                    }
                }
                .superclass(GraphQLInput::class)
                // add a constructor with a parameter for every field
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addAnnotation(JsonCreator::class)
                        .addParameters(
                            fields.map { field ->
                                val type = type(field)
                                ParameterSpec.builder(
                                    name = field.name,
                                    type = type
                                ).addAnnotation(AnnotationSpec.builder(JsonProperty::class).addMember("%S", field.name).build())
                                    .apply {
                                        if (field.defaultValue != null || type.isNullable) {
                                            val value = field.defaultValue?.let {
                                                generateCode(
                                                    it,
                                                    type,
                                                    document
                                                        .getDefinitionsOfType(InputObjectTypeDefinition::class.java),
                                                    config,
                                                    typeUtils
                                                )
                                            }
                                            defaultValue("default<%T, %T>(%S, $value)", typeName, type, field.name)
                                        }
                                    }
                                    .build()
                            }
                        )
                        .build()
                )
                // add a backing property for each field
                .addProperties(
                    fields.map { field ->
                        PropertySpec.builder(
                            name = field.name,
                            type = type(field)
                        )
                            .initializer(field.name)
                            .build()
                    }
                )
                .addFunction(
                    FunSpec.builder("fields")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(
                            List::class.asClassName().parameterizedBy(
                                Pair::class.asClassName().parameterizedBy(
                                    String::class.asClassName(),
                                    Any::class.asClassName().copy(nullable = true)
                                )
                            )
                        )
                        .addCode(
                            fields.let { fs ->
                                val builder = CodeBlock.builder().add("return listOf(")
                                fs.forEachIndexed { i, f -> builder.add("%S to %N%L", f.name, f.name, if (i < fs.size.dec()) ", " else "") }
                                builder.add(")").build()
                            }
                        )
                        .build()
                )
                .build()

            // return a file per type
            FileSpec.get(config.packageNameTypes, typeSpec)
        }
}

private fun checkAndGetLocaleValue(value: StringValue, type: TypeName): String? {
    if (type.className.canonicalName == "java.util.Locale") return "Locale.forLanguageTag(\"${value.value}\")"
    return null
}

private fun generateCode(
    value: Value<Value<*>>,
    type: TypeName,
    inputTypeDefinitions: Collection<InputObjectTypeDefinition>,
    config: CodeGenConfig,
    typeUtils: KotlinTypeUtils
): CodeBlock? {
    return when (value) {
        is BooleanValue -> CodeBlock.of("%L", value.isValue)
        is IntValue -> CodeBlock.of("%L", value.value)
        is StringValue -> {
            val localeValueOverride = checkAndGetLocaleValue(value, type)
            if (localeValueOverride != null) CodeBlock.of("%L", localeValueOverride)
            else CodeBlock.of("%S", value.value)
        }
        is FloatValue -> CodeBlock.of("%L", value.value)
        is EnumValue -> CodeBlock.of("%M", MemberName(type.className, value.name))
        is ArrayValue ->
            if (value.values.isEmpty()) CodeBlock.of("emptyList()")
            else CodeBlock.of(
                "listOf(%L)",
                value.values.joinToString { v -> generateCode(v, type, inputTypeDefinitions, config, typeUtils).toString() }
            )

        is ObjectValue -> {
            val inputObjectDefinition = inputTypeDefinitions.first {
                val expectedCanonicalClassName = config.typeMapping[it.name] ?: "${config.packageNameTypes}.${it.name}"
                expectedCanonicalClassName == type.className.canonicalName
            }

            CodeBlock.of(
                type.className.canonicalName + "(%L)",
                value.objectFields.joinToString { objectProperty ->
                    val argumentType =
                        checkNotNull(inputObjectDefinition.inputValueDefinitions.find { it.name == objectProperty.name }) {
                            "Property \"${objectProperty.name}\" does not exist in input type \"${inputObjectDefinition.name}\""
                        }
                    "${objectProperty.name} = ${
                    generateCode(
                        objectProperty.value,
                        typeUtils.findReturnType(argumentType.type),
                        inputTypeDefinitions,
                        config,
                        typeUtils
                    )
                    }"
                }
            )
        }

        else -> CodeBlock.of("%L", value)
    }
}

private val TypeName.className: ClassName
    get() = when (this) {
        is ClassName -> this
        is ParameterizedTypeName -> typeArguments[0].className
        else -> TODO()
    }
