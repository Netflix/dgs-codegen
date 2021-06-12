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
import com.netflix.graphql.dgs.codegen.filterSkipped
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.TypeName
import graphql.language.*

class KotlinDataTypeGenerator(private val config: CodeGenConfig, private val document: Document) : AbstractKotlinDataTypeGenerator(config.packageNameTypes, config) {
    fun generate(definition: ObjectTypeDefinition, extensions: List<ObjectTypeExtensionDefinition>): CodeGenResult {
        if (definition.shouldSkip()) {
            return CodeGenResult()
        }

        val fields = definition.fieldDefinitions
            .filterSkipped()
            .filter(ReservedKeywordFilter.filterInvalidNames)
            .map { Field(it.name, typeUtils.findReturnType(it.type), typeUtils.isNullable(it.type)) }
            .plus(
                extensions.flatMap { it.fieldDefinitions }
                    .filterSkipped()
                    .map { Field(it.name, typeUtils.findReturnType(it.type), typeUtils.isNullable(it.type)) }
            )
        val interfaces = definition.implements
        return generate(definition.name, fields, interfaces, false, document, definition.description)
    }

    override fun getPackageName(): String {
        return config.packageNameTypes
    }
}

class KotlinInputTypeGenerator(private val config: CodeGenConfig, private val document: Document) : AbstractKotlinDataTypeGenerator(config.packageNameTypes, config) {
    fun generate(definition: InputObjectTypeDefinition, extensions: List<InputObjectTypeExtensionDefinition>): CodeGenResult {

        val fields = definition.inputValueDefinitions
            .filter(ReservedKeywordFilter.filterInvalidNames)
            .map {
                val type = typeUtils.findReturnType(it.type)
                val defaultValue = it.defaultValue?.let { value -> generateCode(value, type) }
                Field(it.name, type, typeUtils.isNullable(it.type), defaultValue)
            }.plus(extensions.flatMap { it.inputValueDefinitions }.map { Field(it.name, typeUtils.findReturnType(it.type), typeUtils.isNullable(it.type)) })
        val interfaces = emptyList<Type<*>>()
        return generate(definition.name, fields, interfaces, true, document, definition.description)
    }

    private fun generateCode(value: Value<Value<*>>, type: TypeName): CodeBlock =
        when (value) {
            is BooleanValue -> CodeBlock.of("%L", value.isValue)
            is IntValue -> CodeBlock.of("%L", value.value)
            is StringValue -> CodeBlock.of("%S", value.value)
            is FloatValue -> CodeBlock.of("%L", value.value)
            is EnumValue -> CodeBlock.of("%M", MemberName(type.className, value.name))
            is ArrayValue ->
                if (value.values.isEmpty()) CodeBlock.of("emptyList()")
                else CodeBlock.of("listOf(%L)", value.values.joinToString { v -> generateCode(v, type).toString() })
            else -> CodeBlock.of("%L", value)
        }

    private val TypeName.className: ClassName
        get() = when (this) {
            is ClassName -> this
            is ParameterizedTypeName -> typeArguments[0].className
            else -> TODO()
        }

    override fun getPackageName(): String {
        return config.packageNameTypes
    }
}

internal data class Field(val name: String, val type: com.squareup.kotlinpoet.TypeName, val nullable: Boolean, val default: CodeBlock? = null)

abstract class AbstractKotlinDataTypeGenerator(private val packageName: String, private val config: CodeGenConfig) {
    protected val typeUtils = KotlinTypeUtils(packageName, config)

    internal fun generate(name: String, fields: List<Field>, interfaces: List<Type<*>>, isInputType: Boolean, document: Document, description: Description? = null): CodeGenResult {
        val kotlinType = TypeSpec.classBuilder(name)

        if (fields.isNotEmpty()) {
            kotlinType.addModifiers(KModifier.DATA)
        }

        if (description != null) {
            kotlinType.addKdoc(description.content.lines().joinToString("\n"))
        }

        val constructorBuilder = FunSpec.constructorBuilder()

        fields.forEach { field ->
            val returnType = if (field.nullable) field.type.copy(nullable = true) else field.type
            val parameterSpec = ParameterSpec.builder(field.name, returnType)
                .addAnnotation(jsonPropertyAnnotation(field.name))

            if (field.default != null) {
                parameterSpec.defaultValue(field.default)
            } else {
                when (returnType) {
                    STRING -> if (field.nullable) parameterSpec.defaultValue("null")
                    INT -> if (field.nullable) parameterSpec.defaultValue("null")
                    FLOAT -> if (field.nullable) parameterSpec.defaultValue("null")
                    DOUBLE -> if (field.nullable) parameterSpec.defaultValue("null")
                    BOOLEAN -> if (field.nullable) parameterSpec.defaultValue("null")
                    else -> if (field.nullable) parameterSpec.defaultValue("null")
                }
            }

            val interfaceNames = interfaces.map { it as NamedNode<*> }.map { it.name }.toSet()
            val interfaceTypes = document.getDefinitionsOfType(InterfaceTypeDefinition::class.java)
            val implementedInterfaces = interfaceTypes.filter { interfaceNames.contains(it.name) }
            val interfaceFields = implementedInterfaces.flatMap { it.fieldDefinitions }.map { it.name }.toSet()

            if (interfaceFields.contains(field.name)) {
                parameterSpec.addModifiers(KModifier.OVERRIDE)
            }

            constructorBuilder.addParameter(parameterSpec.build())
            val propertySpecBuilder = PropertySpec.builder(field.name, returnType)
            propertySpecBuilder.initializer(field.name)
            kotlinType.addProperty(propertySpecBuilder.build())
        }

        val unionTypes = document.getDefinitionsOfType(UnionTypeDefinition::class.java).filter { union ->
            union.memberTypes.map { it as graphql.language.TypeName }.map { it.name }.contains(name)
        }

        val interfaceTypes = interfaces.plus(unionTypes)
        interfaceTypes.forEach {
            if (it is NamedNode<*>) {
                kotlinType.addSuperinterface(ClassName.bestGuess("${getPackageName()}.${it.name}"))
            }
        }

        if (interfaceTypes.isNotEmpty()) {
            kotlinType.addAnnotation(disableJsonTypeInfoAnnotation())
        }

        kotlinType.primaryConstructor(constructorBuilder.build())
        kotlinType.addType(TypeSpec.companionObjectBuilder().build())

        val typeSpec = kotlinType.build()

        val fileSpec = FileSpec.builder(getPackageName(), typeSpec.name!!).addType(typeSpec).build()

        return CodeGenResult(kotlinDataTypes = listOf(fileSpec))
    }

    abstract fun getPackageName(): String
}
