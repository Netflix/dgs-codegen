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
import com.netflix.graphql.dgs.codegen.filterSkipped
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonSubTypesAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonTypeInfoAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.sanitizeKdoc
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInterfaceExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findUnionExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.Document
import graphql.language.InterfaceTypeDefinition
import graphql.language.NamedNode
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import graphql.language.UnionTypeDefinition

fun generateKotlin2Interfaces(
    config: CodeGenConfig,
    document: Document
): List<FileSpec> {

    if (!config.generateDataTypes) {
        return emptyList()
    }

    val typeLookup = Kotlin2TypeLookup(config, document)

    val interfaceClasses = document
        .getDefinitionsOfType(InterfaceTypeDefinition::class.java)
        .excludeSchemaTypeExtension()
        .filter { !it.shouldSkip(config) }
        .map { interfaceDefinition ->

            logger.info("Generating interface type ${interfaceDefinition.name}")

            // get all types that implement this interface
            val implementations = document
                .getDefinitionsOfType(ObjectTypeDefinition::class.java)
                .filter { node -> node.implements.any { it.isEqualTo(TypeName(interfaceDefinition.name)) } }
                .map { node -> ClassName(config.packageNameTypes, node.name) }

            // get all interfaces that this interface implements
            val implementedInterfaces = typeLookup.implementedInterfaces(interfaceDefinition)

            // get any fields defined via schema extensions
            val extensionTypes = findInterfaceExtensions(interfaceDefinition.name, document.definitions)

            // get all fields defined on the type itself or any extension types
            val fields = listOf(interfaceDefinition)
                .plus(extensionTypes)
                .flatMap { it.fieldDefinitions }
                .filterSkipped()

            // get a list of fields to override
            val overrideFields = typeLookup.overrideFields(implementedInterfaces)

            // create the interface
            val interfaceSpec = TypeSpec.interfaceBuilder(interfaceDefinition.name)
                .addModifiers(KModifier.SEALED)
                // add docs if available
                .apply {
                    if (interfaceDefinition.description != null) {
                        addKdoc("%L", interfaceDefinition.description.sanitizeKdoc())
                    }
                }
                // add jackson annotations
                .addAnnotation(jsonTypeInfoAnnotation())
                .apply {
                    if (implementations.isNotEmpty()) {
                        addAnnotation(jsonSubTypesAnnotation(implementations))
                    }
                }
                // add interfaces to implement
                .addSuperinterfaces(
                    implementedInterfaces.map { ClassName(config.packageNameTypes, it) }
                )
                // add fields, overriding if needed
                .addProperties(
                    fields.map { field ->
                        PropertySpec.builder(
                            name = field.name,
                            type = typeLookup.findReturnType(config.packageNameTypes, field.type)
                        )
                            .apply {
                                if (field.description != null) {
                                    addKdoc("%L", field.description.sanitizeKdoc())
                                }
                            }
                            .apply {
                                if (field.name in overrideFields) {
                                    addModifiers(KModifier.OVERRIDE)
                                }
                            }
                            .build()
                    }
                )
                .build()

            // return a file per interface
            FileSpec.get(config.packageNameTypes, interfaceSpec)
        }

    val unionClasses = document.getDefinitionsOfType(UnionTypeDefinition::class.java)
        .excludeSchemaTypeExtension()
        .filter { !it.shouldSkip(config) }
        .map { unionDefinition ->

            logger.info("Generating union type ${unionDefinition.name}")

            // get any members defined via schema extensions
            val extensionTypes = findUnionExtensions(unionDefinition.name, document.definitions)

            // get all types that implement this union
            val implementations = unionDefinition.memberTypes
                .plus(extensionTypes.flatMap { it.memberTypes })
                .filterIsInstance<NamedNode<*>>()
                .map { node -> ClassName(config.packageNameTypes, node.name) }

            // create the interface
            val interfaceSpec = TypeSpec.interfaceBuilder(unionDefinition.name)
                .addModifiers(KModifier.SEALED)
                // add docs if available
                .apply {
                    if (unionDefinition.description != null) {
                        addKdoc("%L", unionDefinition.description.sanitizeKdoc())
                    }
                }
                // add jackson annotations
                .addAnnotation(jsonTypeInfoAnnotation())
                .apply {
                    if (implementations.isNotEmpty()) {
                        addAnnotation(jsonSubTypesAnnotation(implementations))
                    }
                }
                .build()

            // return a file per interface
            FileSpec.get(config.packageNameTypes, interfaceSpec)
        }

    return interfaceClasses.plus(unionClasses)
}
