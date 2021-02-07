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
import com.netflix.graphql.dgs.codegen.KotlinCodeGenResult
import com.netflix.graphql.dgs.codegen.filterSkipped
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.*
import graphql.language.*

class KotlinDataTypeGenerator(private val config: CodeGenConfig, private val document: Document): AbstractKotlinDataTypeGenerator(config) {
    private val typeUtils = KotlinTypeUtils(getPackageName(), config)

    fun generate(definition: ObjectTypeDefinition, extensions: List<ObjectTypeExtensionDefinition>): KotlinCodeGenResult {
        if(definition.shouldSkip()) {
            return KotlinCodeGenResult()
        }

        val fields = definition.fieldDefinitions
                .filterSkipped()
                .filter(ReservedKeywordFilter.filterInvalidNames)
            .map { Field(it.name, typeUtils.findReturnType(it.type), typeUtils.isNullable(it.type)) }
                .plus(extensions.flatMap { it.fieldDefinitions }
                        .filterSkipped()
                        .map { Field(it.name, typeUtils.findReturnType(it.type), typeUtils.isNullable(it.type)) })
        val interfaces = definition.implements
        return generate(definition.name, fields, interfaces, false, document)
    }

    override fun getPackageName(): String {
        return config.packageName + ".types"
    }
}

class KotlinInputTypeGenerator(private val config: CodeGenConfig, private val document: Document): AbstractKotlinDataTypeGenerator(config) {
    private val typeUtils = KotlinTypeUtils(getPackageName(), config)

    fun generate(definition: InputObjectTypeDefinition, extensions: List<InputObjectTypeExtensionDefinition>): KotlinCodeGenResult {

        val fields = definition.inputValueDefinitions
            .filter(ReservedKeywordFilter.filterInvalidNames)
            .map {
            val defaultValue: Any
            if (it.defaultValue != null) {
                defaultValue = when (it.defaultValue) {
                    is BooleanValue -> (it.defaultValue as BooleanValue).isValue
                    is IntValue -> (it.defaultValue as IntValue).value
                    is StringValue -> (it.defaultValue as StringValue).value
                    is FloatValue -> (it.defaultValue as FloatValue).value
                    else -> it.defaultValue
                }

                Field(it.name, typeUtils.findReturnType(it.type), typeUtils.isNullable(it.type), defaultValue)
            } else {
                Field(it.name, typeUtils.findReturnType(it.type), typeUtils.isNullable(it.type))
            }
        }.plus(extensions.flatMap { it.inputValueDefinitions }.map { Field(it.name, typeUtils.findReturnType(it.type), typeUtils.isNullable(it.type)) })
        val interfaces = emptyList<Type<*>>()
        return generate(definition.name, fields, interfaces, true, document)
    }

    override fun getPackageName(): String {
        return config.packageName + ".types"
    }
}

internal data class Field(val name: String, val type: com.squareup.kotlinpoet.TypeName, val nullable: Boolean, val default: Any? = null)

abstract class AbstractKotlinDataTypeGenerator(private val config: CodeGenConfig) {

    internal fun generate(name: String, fields: List<Field>, interfaces: List<Type<*>>, isInputType: Boolean, document: Document): KotlinCodeGenResult {
        val kotlinType = TypeSpec.classBuilder(name)

        if(fields.isNotEmpty()) {
            kotlinType.addModifiers(KModifier.DATA)
        }

        val constructorBuilder = FunSpec.constructorBuilder()

        val interfaceTypes = document.getDefinitionsOfType(InterfaceTypeDefinition::class.java)
        if (interfaceTypes.isNotEmpty()) {
            kotlinType.addAnnotation(disableJsonTypeInfoAnnotation())
        }

        fields.forEach { field ->
            val returnType = if(field.nullable) field.type.copy(nullable = true) else field.type
            val parameterSpec = ParameterSpec.builder(field.name, returnType)
                    .addAnnotation(jsonPropertyAnnotation(field.name))

            if (field.default != null) {
                val initializerBlock = if (field.type.toString().contains("String")) {
                    "\"${field.default}\""
                } else {
                    "${field.default}"
                }
                parameterSpec.defaultValue(initializerBlock)
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

        interfaces.plus(unionTypes).forEach {
            if(it is NamedNode<*>) {
                kotlinType.addSuperinterface(ClassName.bestGuess("${getPackageName()}.${it.name}"))
            }
        }

        kotlinType.primaryConstructor(constructorBuilder.build())
        if (isInputType) {
            kotlinType.addFunction(FunSpec.builder("toString")
                    .returns(STRING)
                    .addCode(addToString(fields, kotlinType))
                    .addModifiers(KModifier.PUBLIC)
                    .addModifiers(KModifier.OVERRIDE)
                    .build())
        }
        val typeSpec = kotlinType.build()

        val fileSpec = FileSpec.builder(getPackageName(), typeSpec.name!!).addType(typeSpec).build()

        return KotlinCodeGenResult(listOf(fileSpec))
    }

    private fun addToString(fields: List<Field>, kotlinType: TypeSpec.Builder): String {
        val toStringBody = StringBuilder("return \"{\" + ")
        fields.mapIndexed { index, field ->
            when (val fieldTypeName = field.type) {
                is ParameterizedTypeName -> {
                    if (fieldTypeName.typeArguments[0] is ClassName && (fieldTypeName.typeArguments[0] as ClassName).simpleName == STRING.simpleName) {
                        addToStringForListOfStrings(field, kotlinType)
                        """
                            "${field.name}:" + serializeListOfStrings(${field.name}) + "${if (index < fields.size - 1) "," else ""}" +
                        """.trimIndent()
                    } else {
                        defaultString(field, index, fields)
                    }
                }

                is ClassName -> {
                    when(fieldTypeName.simpleName) {
                        STRING.simpleName -> {
                            if (field.nullable) {
                                """
                                "${field.name}:" + "${'$'}{if(${field.name} != null) "\"" else ""}" + ${field.name} + "${'$'}{if(${field.name} != null) "\"" else ""}" + "${if (index < fields.size - 1) "," else ""}" +
                            """.trimIndent()
                            } else {
                                """
                                "${field.name}:" + "\"" + ${field.name} + "\"" + "${if (index < fields.size - 1) "," else ""}" +
                            """.trimIndent()
                            }
                        }
                        else -> {
                            defaultString(field, index, fields)
                        }
                    }
                }
                else -> {
                    defaultString(field, index, fields)
                }
            }
        }.forEach { toStringBody.append(it)}

        return toStringBody.append("""
            "}"
        """.trimIndent()).toString()
    }

    private fun defaultString(field: Field, index: Int, fields: List<Field>): String {
        return """
            "${field.name}:" + ${field.name} + "${if (index < fields.size - 1) "," else ""}" +
            """.trimIndent()
    }

    private fun addToStringForListOfStrings(field: Field, kotlinType: TypeSpec.Builder) {
        if (kotlinType.funSpecs.any { it.name == "serializeListOfStrings" }) return

        val methodBuilder = FunSpec.builder("serializeListOfStrings")
                .addModifiers(KModifier.PRIVATE)
                .addParameter(field.name, field.type)
                .returns(STRING.copy(nullable = true))

        val toStringBody = StringBuilder()
        if (field.nullable) {
            toStringBody.append("""
                if (${field.name} == null) {
                    return null
                }
                
            """.trimIndent()
            )
        }
        toStringBody.append(
            """
                val builder = java.lang.StringBuilder()
                builder.append("[")
                if (! ${field.name}.isEmpty()) {
                    val result = ${field.name}.joinToString() {"\"" + it + "\""}
                    builder.append(result)
                }
                builder.append("]")
                return  builder.toString()
            """.trimIndent()
        )


        methodBuilder.addStatement(toStringBody.toString())
        kotlinType.addFunction(methodBuilder.build())
    }

    abstract fun getPackageName(): String
}