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
import com.netflix.graphql.dgs.codegen.generators.java.InputTypeGenerator
import com.netflix.graphql.dgs.codegen.generators.shared.applyDirectivesKotlin
import com.netflix.graphql.dgs.codegen.generators.shared.generateKotlinCode
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Serializable
import com.squareup.kotlinpoet.TypeName as KtTypeName

class KotlinDataTypeGenerator(
    config: CodeGenConfig,
    document: Document,
) : AbstractKotlinDataTypeGenerator(packageName = config.packageNameTypes, config = config, document = document) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(KotlinDataTypeGenerator::class.java)
    }

    fun generate(
        definition: ObjectTypeDefinition,
        extensions: List<ObjectTypeExtensionDefinition>,
    ): CodeGenResult {
        if (definition.shouldSkip(config)) {
            return CodeGenResult.EMPTY
        }

        logger.info("Generating data type {}", definition.name)

        val fields =
            definition.fieldDefinitions
                .filterSkipped()
                .filter(ReservedKeywordFilter.filterInvalidNames)
                .map {
                    Field(
                        it.name,
                        typeUtils.findReturnType(it.type),
                        typeUtils.isNullable(it.type),
                        null,
                        it.description,
                        it.directives,
                    )
                } +
                extensions
                    .flatMap { it.fieldDefinitions }
                    .filterSkipped()
                    .map {
                        Field(
                            it.name,
                            typeUtils.findReturnType(it.type),
                            typeUtils.isNullable(it.type),
                            null,
                            it.description,
                            it.directives,
                        )
                    }
        val interfaces = definition.implements + extensions.flatMap { it.implements }
        return generate(definition.name, fields, interfaces, document, definition.description, definition.directives)
    }
}

class KotlinInputTypeGenerator(
    config: CodeGenConfig,
    document: Document,
) : AbstractKotlinDataTypeGenerator(packageName = config.packageNameTypes, config = config, document = document) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(InputTypeGenerator::class.java)
    }

    fun generate(
        definition: InputObjectTypeDefinition,
        extensions: List<InputObjectTypeExtensionDefinition>,
        inputTypeDefinitions: Collection<InputObjectTypeDefinition>,
    ): CodeGenResult {
        if (definition.shouldSkip(config)) {
            return CodeGenResult.EMPTY
        }

        logger.info("Generating input type {}", definition.name)

        val fields =
            definition.inputValueDefinitions
                .filter(ReservedKeywordFilter.filterInvalidNames)
                .map {
                    val defaultValue =
                        it.defaultValue?.let { value ->
                            generateKotlinCode(
                                value,
                                typeUtils.findReturnType(it.type),
                                inputTypeDefinitions,
                                config,
                                typeUtils,
                            )
                        }
                    Field(
                        name = it.name,
                        type = typeUtils.findReturnType(it.type),
                        nullable = it.type !is NonNullType,
                        default = defaultValue,
                        description = it.description,
                        directives = it.directives,
                    )
                }.plus(
                    extensions.flatMap { it.inputValueDefinitions }.map {
                        Field(
                            name = it.name,
                            type = typeUtils.findReturnType(it.type),
                            nullable = it.type !is NonNullType,
                            default = null,
                            description = it.description,
                            directives = it.directives,
                        )
                    },
                )
        val interfaces = emptyList<Type<*>>()
        return generate(definition.name, fields, interfaces, document, definition.description, definition.directives)
    }
}

internal data class Field(
    val name: String,
    val type: KtTypeName,
    val nullable: Boolean,
    val default: CodeBlock? = null,
    val description: Description? = null,
    val directives: List<Directive> = emptyList(),
)

abstract class AbstractKotlinDataTypeGenerator(
    packageName: String,
    protected val config: CodeGenConfig,
    protected val document: Document,
) {
    protected val typeUtils =
        KotlinTypeUtils(
            packageName = packageName,
            config = config,
            document = document,
        )

    internal fun generate(
        name: String,
        fields: List<Field>,
        interfaces: List<Type<*>>,
        document: Document,
        description: Description? = null,
        directives: List<Directive> = emptyList(),
    ): CodeGenResult {
        val kotlinType =
            TypeSpec
                .classBuilder(name)
                .addOptionalGeneratedAnnotation(config)

        if (config.implementSerializable) {
            kotlinType.addSuperinterface(ClassName.bestGuess(Serializable::class.java.name))
        }

        if (fields.isNotEmpty()) {
            kotlinType.addModifiers(KModifier.DATA)
        }

        if (description != null) {
            kotlinType.addKdoc("%L", description.sanitizeKdoc())
        }

        if (directives.isNotEmpty()) {
            kotlinType.addAnnotations(applyDirectivesKotlin(directives, config))
        }

        val funConstructorBuilder = FunSpec.constructorBuilder()

        fields.forEach { field ->
            val returnType = if (field.nullable) field.type.copy(nullable = true) else field.type

            val parameterSpec =
                ParameterSpec
                    .builder(field.name, returnType)
                    .addAnnotation(jsonPropertyAnnotation(field.name))

            if (field.directives.isNotEmpty()) {
                parameterSpec.addAnnotations(applyDirectivesKotlin(field.directives, config))
            }

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
            funConstructorBuilder.addParameter(parameterSpec.build())
        }
        kotlinType.primaryConstructor(funConstructorBuilder.build())

        fields.forEach { field ->
            val returnType = if (field.nullable) field.type.copy(nullable = true) else field.type
            val propertySpecBuilder = PropertySpec.builder(field.name, returnType)

            if (field.description != null) {
                propertySpecBuilder.addKdoc("%L", field.description.sanitizeKdoc())
            }
            propertySpecBuilder.initializer(field.name)

            val interfaceNames =
                interfaces
                    .asSequence()
                    .map { it as NamedNode<*> }
                    .map { it.name }
                    .toSet()
            val interfaceTypes = document.getDefinitionsOfType(InterfaceTypeDefinition::class.java)
            val implementedInterfaces = interfaceTypes.filter { interfaceNames.contains(it.name) }
            val interfaceFields =
                implementedInterfaces
                    .asSequence()
                    .flatMap { it.fieldDefinitions }
                    .map { it.name }
                    .toSet()

            if (field.name in interfaceFields) {
                // Properties are the syntactical element that will allow us to override things, they are the spec on
                // which we should add the override modifier.
                propertySpecBuilder.addModifiers(KModifier.OVERRIDE)
            }

            kotlinType.addProperty(propertySpecBuilder.build())
        }

        val unionTypes =
            document.getDefinitionsOfType(UnionTypeDefinition::class.java).filter { union ->
                union.memberTypes
                    .asSequence()
                    .map { it as TypeName }
                    .any { it.name == name }
            }

        val interfaceTypes = interfaces + unionTypes
        interfaceTypes.forEach {
            if (it is NamedNode<*>) {
                kotlinType.addSuperinterface(typeUtils.findKtInterfaceName(it.name, getPackageName()))
            }
        }

        if (interfaceTypes.isNotEmpty()) {
            kotlinType.addAnnotation(disableJsonTypeInfoAnnotation())
        }

        kotlinType.primaryConstructor(funConstructorBuilder.build())
        kotlinType.addType(TypeSpec.companionObjectBuilder().addOptionalGeneratedAnnotation(config).build())

        val typeSpec = kotlinType.build()

        val fileSpec = FileSpec.builder(getPackageName(), typeSpec.name!!).addType(typeSpec).build()

        return CodeGenResult(kotlinDataTypes = listOf(fileSpec))
    }

    open fun getPackageName(): String = config.packageNameTypes
}
