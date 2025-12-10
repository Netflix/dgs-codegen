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
import com.palantir.javapoet.ClassName
import com.palantir.javapoet.FieldSpec
import com.palantir.javapoet.JavaFile
import com.palantir.javapoet.TypeSpec
import graphql.language.*
import javax.lang.model.element.Modifier

class ConstantsGenerator(
    private val config: CodeGenConfig,
    private val document: Document,
) {
    private val javaReservedKeywordSanitizer = JavaReservedKeywordSanitizer()

    class TypeSpecBuilderWrapper(
        private val builder: TypeSpec.Builder,
    ) {
        private val _fieldNames = mutableListOf<String>()
        val fieldNames: List<String>
            get() = _fieldNames

        private val _typeNames = mutableListOf<String>()
        val typeNames: List<String>
            get() = _typeNames

        fun addField(fieldSpec: FieldSpec) {
            _fieldNames.add(fieldSpec.name())
            builder.addField(fieldSpec)
        }

        fun addType(typeSpec: TypeSpec) {
            _typeNames.add(typeSpec.name())
            builder.addType(typeSpec)
        }

        fun build(): TypeSpec = builder.build()
    }

    fun generate(): CodeGenResult {
        val javaType =
            TypeSpec
                .classBuilder("DgsConstants")
                .addOptionalGeneratedAnnotation(config)
                .addModifiers(Modifier.PUBLIC)

        val types = mutableMapOf<String, TypeSpecBuilderWrapper>()

        document.definitions
            .filterIsInstance<ObjectTypeDefinition>()
            .asSequence()
            .excludeSchemaTypeExtension()
            .forEach {
                val constantsType =
                    getOrCreateConstantsType(types, it.name)

                val extensions = findTypeExtensions(it.name, document.definitions)
                val fields = it.fieldDefinitions + extensions.flatMap { ext -> ext.fieldDefinitions }

                if (!types.contains(it.name)) {
                    constantsType.addField(
                        FieldSpec
                            .builder(ClassName.get(String::class.java), "TYPE_NAME")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("\$S", it.name)
                            .build(),
                    )
                }

                fields.forEach { field ->
                    addFieldNameConstant(constantsType, field.name)
                    addQueryInputArgument(constantsType, field)
                }

                types[it.name] = constantsType
            }

        document.definitions
            .filterIsInstance<InputObjectTypeDefinition>()
            .asSequence()
            .excludeSchemaTypeExtension()
            .forEach {
                val constantsType =
                    getOrCreateConstantsType(types, it.name)

                if (!types.contains(it.name)) {
                    constantsType.addField(
                        FieldSpec
                            .builder(ClassName.get(String::class.java), "TYPE_NAME")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("\$S", it.name)
                            .build(),
                    )
                }

                for (definition in it.inputValueDefinitions) {
                    addFieldNameConstant(constantsType, definition.name)
                }

                val extensions = findInputExtensions(it.name, document.definitions)
                for (extension in extensions) {
                    for (definition in extension.inputValueDefinitions) {
                        addFieldNameConstant(constantsType, definition.name)
                    }
                }

                types[it.name] = constantsType
            }

        document.definitions
            .filterIsInstance<InterfaceTypeDefinition>()
            .asSequence()
            .excludeSchemaTypeExtension()
            .forEach {
                val constantsType =
                    getOrCreateConstantsType(types, it.name)

                if (!types.contains(it.name)) {
                    constantsType.addField(
                        FieldSpec
                            .builder(ClassName.get(String::class.java), "TYPE_NAME")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("\$S", it.name)
                            .build(),
                    )
                }

                for (definition in it.fieldDefinitions) {
                    addFieldNameConstant(constantsType, definition.name)
                }

                val extensions = findInterfaceExtensions(it.name, document.definitions)
                for (extension in extensions) {
                    for (definition in extension.fieldDefinitions) {
                        addFieldNameConstant(constantsType, definition.name)
                    }
                }

                types[it.name] = constantsType
            }

        document.definitions
            .filterIsInstance<UnionTypeDefinition>()
            .asSequence()
            .excludeSchemaTypeExtension()
            .forEach {
                val constantsType = createConstantTypeBuilder(config, it.name)
                constantsType.addField(
                    FieldSpec
                        .builder(ClassName.get(String::class.java), "TYPE_NAME")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("\$S", it.name)
                        .build(),
                )
            }

        types.values.forEach {
            javaType.addType(it.build())
        }

        if (document.definitions.any { it is ObjectTypeDefinition && it.name == "Query" }) {
            javaType.addField(
                FieldSpec
                    .builder(ClassName.get(String::class.java), "QUERY_TYPE")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(""""Query"""")
                    .build(),
            )
        }
        if (document.definitions.any { it is ObjectTypeDefinition && it.name == "Mutation" }) {
            javaType.addField(
                FieldSpec
                    .builder(ClassName.get(String::class.java), "MUTATION_TYPE")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(""""Mutation"""")
                    .build(),
            )
        }
        if (document.definitions.any { it is ObjectTypeDefinition && it.name == "Subscription" }) {
            javaType.addField(
                FieldSpec
                    .builder(ClassName.get(String::class.java), "SUBSCRIPTION_TYPE")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(""""Subscription"""")
                    .build(),
            )
        }

        val javaFile = JavaFile.builder(config.packageName, javaType.build()).build()
        return CodeGenResult(javaConstants = listOf(javaFile))
    }

    private fun getOrCreateConstantsType(
        types: Map<String, TypeSpecBuilderWrapper>,
        name: String,
    ): TypeSpecBuilderWrapper =
        if (types.contains(name)) {
            types[name]!!
        } else {
            createConstantTypeBuilder(config, name)
        }

    private fun createConstantTypeBuilder(
        conf: CodeGenConfig,
        name: String,
    ): TypeSpecBuilderWrapper {
        val className =
            getConstantTypeName(conf, name)

        return TypeSpecBuilderWrapper(
            builder =
                TypeSpec
                    .classBuilder(className)
                    .addOptionalGeneratedAnnotation(config)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC),
        )
    }

    private fun getConstantTypeName(
        conf: CodeGenConfig,
        name: String,
    ): String {
        val className =
            if (conf.snakeCaseConstantNames) {
                CodeGeneratorUtils.camelCaseToSnakeCase(name, CodeGeneratorUtils.Case.UPPERCASE)
            } else {
                name.uppercase()
            }
        return className
    }

    private fun addFieldNameConstant(
        constantsType: TypeSpecBuilderWrapper,
        fieldName: String,
    ) {
        val sanitizedFieldName = javaReservedKeywordSanitizer.sanitize(fieldName.capitalized())
        if (!constantsType.fieldNames.any { it == sanitizedFieldName }) {
            constantsType.addField(
                FieldSpec
                    .builder(
                        ClassName.get(String::class.java),
                        sanitizedFieldName,
                    ).addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\$S", fieldName)
                    .build(),
            )
        }
    }

    private fun addQueryInputArgument(
        constantsType: TypeSpecBuilderWrapper,
        field: FieldDefinition,
    ) {
        val inputFields = field.inputValueDefinitions
        val name = getConstantTypeName(config, field.name + "_INPUT_ARGUMENT")
        if (inputFields.isNotEmpty() && !constantsType.typeNames.any { it == name }) {
            val inputConstantsType = createConstantTypeBuilder(config, name)
            inputFields.forEach { inputField ->
                addFieldNameConstant(inputConstantsType, inputField.name)
            }

            constantsType.addType(inputConstantsType.build())
        }
    }
}
