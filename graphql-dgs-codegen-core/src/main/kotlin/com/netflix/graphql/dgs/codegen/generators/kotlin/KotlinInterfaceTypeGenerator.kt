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
import graphql.language.*
import graphql.language.TypeName
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KotlinInterfaceTypeGenerator(
    private val config: CodeGenConfig,
    private val document: Document,
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(KotlinInterfaceTypeGenerator::class.java)
    }

    private val packageName = config.packageNameTypes
    private val typeUtils = KotlinTypeUtils(packageName, config, document)

    fun generate(
        definition: InterfaceTypeDefinition,
        extensions: List<InterfaceTypeExtensionDefinition>,
    ): CodeGenResult {
        if (definition.shouldSkip(config)) {
            return CodeGenResult.EMPTY
        }

        logger.info("Generating type {}", definition.name)

        val interfaceBuilder =
            TypeSpec
                .interfaceBuilder(definition.name)
                .addOptionalGeneratedAnnotation(config)
        if (definition.description != null) {
            interfaceBuilder.addKdoc("%L", definition.description.sanitizeKdoc())
        }

        superInterfacesNames(definition)
            .forEach {
                interfaceBuilder.addSuperinterface(typeUtils.findKtInterfaceName(it, packageName))
            }

        val mergedFieldDefinitions = definition.fieldDefinitions + extensions.flatMap { it.fieldDefinitions }

        mergedFieldDefinitions
            .filterSkipped()
            .filter(ReservedKeywordFilter.filterInvalidNames)
            .forEach { field ->
            val returnType = typeUtils.findReturnType(field.type)
            val nullableType = if (typeUtils.isNullable(field.type)) returnType.copy(nullable = true) else returnType
            val kotlinName = sanitizeKotlinIdentifier(field.name)
            val propertySpec = PropertySpec.builder(kotlinName, nullableType)
            if (field.description != null) {
                propertySpec.addKdoc("%L", field.description.sanitizeKdoc())
            }

            if (definition.implements.isNotEmpty()) {
                val superInterfaceFields =
                    document
                        .getDefinitionsOfType(InterfaceTypeDefinition::class.java)
                        .filter {
                            superInterfacesNames(definition).contains(it.name)
                        }.asSequence()
                        .flatMap { it.fieldDefinitions }
                        .map { it.name }
                        .toSet()

                if (field.name in superInterfaceFields) {
                    propertySpec.addModifiers(KModifier.OVERRIDE)
                }
            }

            interfaceBuilder.addProperty(propertySpec.build())
        }

        val implementations =
            document
                .getDefinitionsOfType(ObjectTypeDefinition::class.java)
                .asSequence()
                .filter { node -> node.implements.any { it.isEqualTo(TypeName(definition.name)) } }
                .map { node -> typeUtils.findKtInterfaceName(node.name, packageName) }
                .filterIsInstance<ClassName>()
                .toList()

        if (implementations.isNotEmpty()) {
            interfaceBuilder.addAnnotation(jsonTypeInfoAnnotation())
            interfaceBuilder.addAnnotation(jsonSubTypesAnnotation(implementations))
        }

        interfaceBuilder.addType(TypeSpec.companionObjectBuilder().addOptionalGeneratedAnnotation(config).build())

        val fileSpec = FileSpec.get(packageName, interfaceBuilder.build())
        return CodeGenResult(kotlinInterfaces = listOf(fileSpec))
    }

    private fun superInterfacesNames(definition: InterfaceTypeDefinition): List<String> =
        definition.implements
            .filterIsInstance<TypeName>()
            .map { it.name }
}
