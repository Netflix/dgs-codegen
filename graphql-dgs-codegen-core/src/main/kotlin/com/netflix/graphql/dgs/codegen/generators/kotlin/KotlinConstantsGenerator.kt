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

package com.netflix.graphql.dgs.codegen.generators.kotlin

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInputExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findTypeExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.*

class KotlinConstantsGenerator(private val config: CodeGenConfig, private val document: Document) {
    fun generate(): CodeGenResult {
        val baseConstantsType = TypeSpec.objectBuilder("DgsConstants")

        document.definitions.filterIsInstance<ObjectTypeDefinition>()
            .excludeSchemaTypeExtension()
            .forEach {
                val constantsType = createConstantTypeBuilder(config, it.name)

                val extensions = findTypeExtensions(it.name, document.definitions)
                val fields = (it.fieldDefinitions + extensions.flatMap { ext -> ext.fieldDefinitions })
                    .distinctBy { def -> def.name }

                constantsType.addProperty(PropertySpec.builder("TYPE_NAME", String::class).addModifiers(KModifier.CONST).initializer(""""${it.name}"""").build())

                fields.filter(ReservedKeywordFilter.filterInvalidNames).forEach { field ->
                    addFieldName(constantsType, field.name)
                    addQueryInputArgument(constantsType, field)
                }

                baseConstantsType.addType(constantsType.build())
            }

        document.definitions.filterIsInstance<InputObjectTypeDefinition>()
            .excludeSchemaTypeExtension()
            .forEach {
                val constantsType = createConstantTypeBuilder(config, it.name)

                val extensions = findInputExtensions(it.name, document.definitions)
                val fields = it.inputValueDefinitions + extensions.flatMap { ext -> ext.inputValueDefinitions }
                constantsType.addProperty(PropertySpec.builder("TYPE_NAME", String::class).addModifiers(KModifier.CONST).initializer(""""${it.name}"""").build())
                fields.filter(ReservedKeywordFilter.filterInvalidNames).forEach { field ->
                    addFieldName(constantsType, field.name)
                }

                baseConstantsType.addType(constantsType.build())
            }

        document.definitions.filterIsInstance<InterfaceTypeDefinition>()
            .excludeSchemaTypeExtension()
            .forEach {
                val constantsType = createConstantTypeBuilder(config, it.name)

                constantsType.addProperty(PropertySpec.builder("TYPE_NAME", String::class).addModifiers(KModifier.CONST).initializer(""""${it.name}"""").build())

                val extensions = SchemaExtensionsUtils.findInterfaceExtensions(it.name, document.definitions)
                val fields = it.fieldDefinitions + extensions.flatMap { ext -> ext.fieldDefinitions }

                fields.filter(ReservedKeywordFilter.filterInvalidNames).forEach { field ->
                    addFieldName(constantsType, field.name)
                }

                baseConstantsType.addType(constantsType.build())
            }

        document.definitions.filterIsInstance<UnionTypeDefinition>()
            .excludeSchemaTypeExtension()
            .forEach {
                val constantsType = createConstantTypeBuilder(config, it.name)

                constantsType.addProperty(PropertySpec.builder("TYPE_NAME", String::class).addModifiers(KModifier.CONST).initializer(""""${it.name}"""").build())
                baseConstantsType.addType(constantsType.build())
            }

        if (document.definitions.any { it is ObjectTypeDefinition && it.name == "Query" }) {
            baseConstantsType.addProperty(PropertySpec.builder("QUERY_TYPE", String::class).addModifiers(KModifier.CONST).initializer(""""Query"""").build())
        }

        if (document.definitions.any { it is ObjectTypeDefinition && it.name == "Mutation" }) {
            baseConstantsType.addProperty(PropertySpec.builder("Mutation_TYPE", String::class).addModifiers(KModifier.CONST).initializer(""""Mutation"""").build())
        }

        if (document.definitions.any { it is ObjectTypeDefinition && it.name == "Subscription" }) {
            baseConstantsType.addProperty(PropertySpec.builder("Subscription_TYPE", String::class).addModifiers(KModifier.CONST).initializer(""""Subscription"""").build())
        }

        val fileSpec = FileSpec.builder(config.packageName, "DgsConstants").addType(baseConstantsType.build()).build()
        return CodeGenResult(kotlinConstants = listOf(fileSpec))
    }

    private fun createConstantTypeBuilder(conf: CodeGenConfig, name: String): TypeSpec.Builder {
        val className =
            if (conf.snakeCaseConstantNames) {
                CodeGeneratorUtils.camelCaseToSnakeCase(name, CodeGeneratorUtils.Case.UPPERCASE)
            } else {
                name.uppercase()
            }

        return TypeSpec.objectBuilder(className)
    }

    private fun addFieldName(constantsType: TypeSpec.Builder, name: String) {
        constantsType.addProperty(
            PropertySpec.builder(name.capitalized(), String::class).addModifiers(KModifier.CONST)
                .initializer(""""$name"""").build()
        )
    }

    private fun addQueryInputArgument(constantsType: TypeSpec.Builder, field: FieldDefinition) {
        val inputFields = field.inputValueDefinitions
        if (inputFields.isNotEmpty()) {
            val inputConstantsType = createConstantTypeBuilder(config, field.name + "_INPUT_ARGUMENT")
            inputFields.forEach { inputField ->
                addFieldName(inputConstantsType, inputField.name)
            }
            constantsType.addType(inputConstantsType.build())
        }
    }
}
