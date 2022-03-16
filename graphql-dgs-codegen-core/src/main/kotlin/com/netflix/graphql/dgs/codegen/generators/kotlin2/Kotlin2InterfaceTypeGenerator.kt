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
import com.netflix.graphql.dgs.codegen.generators.kotlin.KotlinTypeUtils
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonSubTypesAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.jsonTypeInfoAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.sanitizeKdoc
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.Document
import graphql.language.InterfaceTypeDefinition
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Kotlin2InterfaceTypeGenerator {

    companion object {

        private val logger: Logger = LoggerFactory.getLogger(Kotlin2InterfaceTypeGenerator::class.java)

        fun generate(
            config: CodeGenConfig,
            document: Document,
        ): List<FileSpec> {

            if (!config.generateDataTypes && !config.generateInterfaces) {
                return emptyList()
            }

            val typeUtils = KotlinTypeUtils(config.packageNameTypes, config)

            // get a map of all interfaces > fields
            val interfaceFields = interfaceFields(document)

            return document
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
                    val implementedInterfaces = implementedInterfaces(interfaceDefinition)

                    // get any fields defined via schema extensions
                    val extensionTypes =
                        SchemaExtensionsUtils.findInterfaceExtensions(interfaceDefinition.name, document.definitions)

                    // get all fields defined on the type itself or any extension types
                    val fields = listOf(interfaceDefinition)
                        .plus(extensionTypes)
                        .flatMap { it.fieldDefinitions }

                    // get a list of fields to override
                    val overrideFields = overrideFields(interfaceFields, implementedInterfaces)

                    // create the interface
                    val interfaceSpec = TypeSpec.interfaceBuilder(interfaceDefinition.name)
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
                                    type = typeUtils.findReturnType(field.type)
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
        }
    }
}