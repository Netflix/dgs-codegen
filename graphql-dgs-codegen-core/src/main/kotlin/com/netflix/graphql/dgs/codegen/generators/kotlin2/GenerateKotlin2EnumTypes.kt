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
import com.netflix.graphql.dgs.codegen.generators.kotlin.addEnumConstants
import com.netflix.graphql.dgs.codegen.generators.kotlin.addOptionalGeneratedAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.sanitizeKdoc
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findEnumExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.applyDirectivesKotlin
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.Document
import graphql.language.EnumTypeDefinition

fun generateKotlin2EnumTypes(
    config: CodeGenConfig,
    document: Document,
    requiredTypes: Set<String>
): List<FileSpec> {
    return document
        .getDefinitionsOfType(EnumTypeDefinition::class.java)
        .excludeSchemaTypeExtension()
        .filter { config.generateDataTypes || it.name in requiredTypes }
        .filter { !it.shouldSkip(config) }
        .map { enumDefinition ->

            logger.info("Generating enum type ${enumDefinition.name}")

            // get any fields defined via schema extensions
            val extensionTypes = findEnumExtensions(enumDefinition.name, document.definitions)

            // get all fields defined on the type itself or any extension types
            val fields = listOf(enumDefinition)
                .plus(extensionTypes)
                .flatMap { it.enumValueDefinitions }

             val companionObject = TypeSpec.companionObjectBuilder()
                .addOptionalGeneratedAnnotation(config)
                .build()

            // create the enum class
            val enumSpec = TypeSpec.classBuilder(enumDefinition.name)
                .addOptionalGeneratedAnnotation(config)
                .addModifiers(KModifier.ENUM)
                // add docs if available
                .apply {
                    if (enumDefinition.description != null) {
                        addKdoc("%L", enumDefinition.description.sanitizeKdoc())
                    }
                }
                // add all fields
                .addEnumConstants(
                    fields.map { field ->
                        TypeSpec.enumBuilder(field.name)
                            .addOptionalGeneratedAnnotation(config)
                            .apply {
                                if (field.description != null) {
                                    addKdoc("%L", field.description.sanitizeKdoc())
                                }
                                if (field.directives.isNotEmpty()) {
                                    addAnnotations(applyDirectivesKotlin(field.directives, config))
                                }
                            }
                            .build()
                    }
                )
                .addType(companionObject)
                .build()

            // return a file per enum
            FileSpec.get(config.packageNameTypes, enumSpec)
        }
}
