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
import com.netflix.graphql.dgs.codegen.GraphQLInput
import com.netflix.graphql.dgs.codegen.generators.kotlin.ReservedKeywordFilter
import com.netflix.graphql.dgs.codegen.generators.kotlin.addOptionalGeneratedAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.sanitizeKdoc
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInputExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import graphql.language.Document
import graphql.language.InputObjectTypeDefinition
import graphql.language.InputValueDefinition

fun generateKotlin2InputTypes(
    config: CodeGenConfig,
    document: Document,
    requiredTypes: Set<String>
): List<FileSpec> {
    val typeLookup = Kotlin2TypeLookup(config, document)

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
                        .addParameters(
                            fields.map { field ->
                                val type = type(field)
                                ParameterSpec.builder(
                                    name = field.name,
                                    type = type
                                )
                                    .apply {
                                        if (type.isNullable) {
                                            defaultValue("default<%T, %T>(%S)", typeName, type, field.name)
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
