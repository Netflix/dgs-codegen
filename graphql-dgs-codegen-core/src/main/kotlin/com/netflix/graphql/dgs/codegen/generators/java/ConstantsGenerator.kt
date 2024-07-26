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

package com.netflix.graphql.dgs.codegen.generators.java

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInputExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInterfaceExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findTypeExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import graphql.language.*
import javax.lang.model.element.Modifier

class ConstantsGenerator(private val config: CodeGenConfig, private val document: Document) {
    fun generate(): CodeGenResult {
        val javaType = TypeSpec.classBuilder("DgsConstants")
            .addOptionalGeneratedAnnotation(config)
            .addModifiers(Modifier.PUBLIC)

        document.definitions.filterIsInstance<ObjectTypeDefinition>()
            .asSequence()
            .excludeSchemaTypeExtension()
            .forEach {
                val constantsType = createConstantTypeBuilder(config, it.name)

                val extensions = findTypeExtensions(it.name, document.definitions)
                val fields = it.fieldDefinitions + extensions.flatMap { ext -> ext.fieldDefinitions }

                constantsType.addField(
                    FieldSpec.builder(ClassName.get(String::class.java), "TYPE_NAME")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("\$S", it.name).build()
                )

                fields.forEach { field ->
                    addFieldNameConstant(constantsType, field.name)
                    addQueryInputArgument(constantsType, field)
                }

                javaType.addType(constantsType.build())
            }

        document.definitions.filterIsInstance<InputObjectTypeDefinition>()
            .asSequence()
            .excludeSchemaTypeExtension()
            .forEach {
                val constantsType = createConstantTypeBuilder(config, it.name)
                constantsType.addField(
                    FieldSpec.builder(ClassName.get(String::class.java), "TYPE_NAME")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("\$S", it.name).build()
                )

                for (definition in it.inputValueDefinitions) {
                    addFieldNameConstant(constantsType, definition.name)
                }

                val extensions = findInputExtensions(it.name, document.definitions)
                for (extension in extensions) {
                    for (definition in extension.inputValueDefinitions) {
                        addFieldNameConstant(constantsType, definition.name)
                    }
                }

                javaType.addType(constantsType.build())
            }

        document.definitions.filterIsInstance<InterfaceTypeDefinition>()
            .asSequence()
            .excludeSchemaTypeExtension()
            .forEach {
                val constantsType = createConstantTypeBuilder(config, it.name)

                constantsType.addField(
                    FieldSpec.builder(ClassName.get(String::class.java), "TYPE_NAME")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("\$S", it.name).build()
                )

                for (definition in it.fieldDefinitions) {
                    addFieldNameConstant(constantsType, definition.name)
                }

                val extensions = findInterfaceExtensions(it.name, document.definitions)
                for (extension in extensions) {
                    for (definition in extension.fieldDefinitions) {
                        addFieldNameConstant(constantsType, definition.name)
                    }
                }

                javaType.addType(constantsType.build())
            }

        document.definitions.filterIsInstance<UnionTypeDefinition>()
            .asSequence()
            .excludeSchemaTypeExtension()
            .forEach {
                val constantsType = createConstantTypeBuilder(config, it.name)
                constantsType.addField(
                    FieldSpec.builder(ClassName.get(String::class.java), "TYPE_NAME")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("\$S", it.name).build()
                )
            }

        if (document.definitions.any { it is ObjectTypeDefinition && it.name == "Query" }) {
            javaType.addField(
                FieldSpec.builder(ClassName.get(String::class.java), "QUERY_TYPE")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(""""Query"""").build()
            )
        }
        if (document.definitions.any { it is ObjectTypeDefinition && it.name == "Mutation" }) {
            javaType.addField(
                FieldSpec.builder(ClassName.get(String::class.java), "MUTATION_TYPE")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(""""Mutation"""").build()
            )
        }
        if (document.definitions.any { it is ObjectTypeDefinition && it.name == "Subscription" }) {
            javaType.addField(
                FieldSpec.builder(ClassName.get(String::class.java), "SUBSCRIPTION_TYPE")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(""""Subscription"""").build()
            )
        }

        val javaFile = JavaFile.builder(config.packageName, javaType.build()).build()
        return CodeGenResult(javaConstants = listOf(javaFile))
    }

    private fun createConstantTypeBuilder(conf: CodeGenConfig, name: String): TypeSpec.Builder {
        val className =
            if (conf.snakeCaseConstantNames) {
                CodeGeneratorUtils.camelCaseToSnakeCase(name, CodeGeneratorUtils.Case.UPPERCASE)
            } else {
                name.uppercase()
            }

        return TypeSpec
            .classBuilder(className)
            .addOptionalGeneratedAnnotation(config)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    }

    private fun addFieldNameConstant(constantsType: TypeSpec.Builder, fieldName: String) {
        constantsType.addField(
            FieldSpec.builder(
                ClassName.get(String::class.java),
                ReservedKeywordSanitizer.sanitize(fieldName.capitalized())
            )
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer("\$S", fieldName).build()
        )
    }

    private fun addQueryInputArgument(constantsType: TypeSpec.Builder, field: FieldDefinition) {
        val inputFields = field.inputValueDefinitions
        if (inputFields.isNotEmpty()) {
            val inputConstantsType = createConstantTypeBuilder(config, field.name + "_INPUT_ARGUMENT")
            inputFields.forEach { inputField ->
                addFieldNameConstant(inputConstantsType, inputField.name)
            }
            constantsType.addType(inputConstantsType.build())
        }
    }
}
