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
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInputExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInterfaceExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findTypeExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import graphql.language.*
import javax.lang.model.element.Modifier

class ConstantsGenerator(private val config: CodeGenConfig, private val document: Document) {
    fun generate(): CodeGenResult {
        val javaType = TypeSpec.classBuilder("DgsConstants")
            .addModifiers(Modifier.PUBLIC)

        document.definitions.filterIsInstance<ObjectTypeDefinition>()
            .excludeSchemaTypeExtension()
            .map {
                val constantsType = createConstantTypeBuilder(config, it.name)

                val extensions = findTypeExtensions(it.name, document.definitions)
                val fields = it.fieldDefinitions + extensions.flatMap { it.fieldDefinitions }

                constantsType.addField(FieldSpec.builder(TypeName.get(String::class.java), "TYPE_NAME").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""${it.name}"""").build())

                fields.forEach { field ->
                    addFieldNameConstant(constantsType, field.name)
                }

                javaType.addType(constantsType.build())
            }

        document.definitions.filterIsInstance<InputObjectTypeDefinition>()
            .excludeSchemaTypeExtension()
            .map {
                val constantsType = createConstantTypeBuilder(config, it.name)
                constantsType.addField(FieldSpec.builder(TypeName.get(String::class.java), "TYPE_NAME").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""${it.name}"""").build())

                val extensions = findInputExtensions(it.name, document.definitions)
                val fields = it.inputValueDefinitions + extensions.flatMap { it.inputValueDefinitions }

                fields.forEach { field ->
                    addFieldNameConstant(constantsType, field.name)
                }

                javaType.addType(constantsType.build())
            }

        document.definitions.filterIsInstance<InterfaceTypeDefinition>()
            .excludeSchemaTypeExtension()
            .map {
                val constantsType = createConstantTypeBuilder(config, it.name)

                constantsType.addField(FieldSpec.builder(TypeName.get(String::class.java), "TYPE_NAME").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""${it.name}"""").build())

                val extensions = findInterfaceExtensions(it.name, document.definitions)
                val merged = it.fieldDefinitions + extensions.flatMap { it.fieldDefinitions }

                merged.forEach { field ->
                    addFieldNameConstant(constantsType, field.name)
                }

                javaType.addType(constantsType.build())
            }

        document.definitions.filterIsInstance<UnionTypeDefinition>()
            .excludeSchemaTypeExtension()
            .map {
                val constantsType = createConstantTypeBuilder(config, it.name)
                constantsType.addField(FieldSpec.builder(TypeName.get(String::class.java), "TYPE_NAME").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""${it.name}"""").build())
            }

        if (document.definitions.firstOrNull { it is ObjectTypeDefinition && it.name == "Query" } != null) {
            javaType.addField(FieldSpec.builder(TypeName.get(String::class.java), "QUERY_TYPE").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""Query"""").build())
        }
        if (document.definitions.firstOrNull { it is ObjectTypeDefinition && it.name == "MUTATION" } != null) {
            javaType.addField(FieldSpec.builder(TypeName.get(String::class.java), "MUTATION_TYPE").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""Mutation"""").build())
        }
        if (document.definitions.firstOrNull { it is ObjectTypeDefinition && it.name == "Subscription" } != null) {
            javaType.addField(FieldSpec.builder(TypeName.get(String::class.java), "SUBSCRIPTION_TYPE").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""Subscription"""").build())
        }

        val javaFile = JavaFile.builder(config.packageName, javaType.build()).build()
        return CodeGenResult(javaConstants = listOf(javaFile))
    }

    private fun createConstantTypeBuilder(conf: CodeGenConfig, name: String): TypeSpec.Builder {
        val className =
            if (conf.snakeCaseConstantNames)
                CodeGeneratorUtils.camelCaseToSnakeCase(name, CodeGeneratorUtils.Case.UPPERCASE)
            else
                name.toUpperCase()

        return TypeSpec
            .classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    }

    private fun addFieldNameConstant(constantsType: TypeSpec.Builder, fieldName: String) {
        constantsType.addField(FieldSpec.builder(TypeName.get(String::class.java), ReservedKeywordSanitizer.sanitize(fieldName.capitalize())).addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""$fieldName"""").build())
    }
}
