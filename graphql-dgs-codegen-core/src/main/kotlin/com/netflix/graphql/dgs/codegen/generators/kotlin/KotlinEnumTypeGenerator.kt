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
import com.netflix.graphql.dgs.codegen.generators.java.EnumTypeGenerator
import com.netflix.graphql.dgs.codegen.generators.java.ReservedKeywordSanitizer
import com.netflix.graphql.dgs.codegen.generators.shared.applyDirectivesKotlin
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.EnumTypeDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KotlinEnumTypeGenerator(private val config: CodeGenConfig) {
    private val logger: Logger = LoggerFactory.getLogger(EnumTypeGenerator::class.java)

    fun generate(definition: EnumTypeDefinition, extensions: List<EnumTypeDefinition>): CodeGenResult {
        if (definition.shouldSkip(config)) {
            return CodeGenResult()
        }

        logger.info("Generating enum type ${definition.name}")

        val kotlinType = TypeSpec.classBuilder(definition.name)
            .addOptionalGeneratedAnnotation(config)
            .addModifiers(KModifier.ENUM)

        if (definition.description != null) {
            kotlinType.addKdoc("%L", definition.description.sanitizeKdoc())
        }

        val mergedEnumDefinitions = definition.enumValueDefinitions + extensions.flatMap { it.enumValueDefinitions }
        mergedEnumDefinitions.forEach {
            var typeSpec = TypeSpec.anonymousClassBuilder()
            if (it.description != null) {
                typeSpec = TypeSpec.enumBuilder(it.name).addKdoc("%L", it.description.sanitizeKdoc())
            }
            if (it.directives.isNotEmpty()) {
                typeSpec = typeSpec.addAnnotations(
                    applyDirectivesKotlin(it.directives, config)
                )
            }
            kotlinType.addEnumConstant(ReservedKeywordSanitizer.sanitize(it.name), typeSpec.build())
        }

        kotlinType.addType(TypeSpec.companionObjectBuilder().addOptionalGeneratedAnnotation(config).build())

        val typeSpec = kotlinType.build()
        val fileSpec = FileSpec.builder(getPackageName(), typeSpec.name!!).addType(typeSpec).build()
        return CodeGenResult(kotlinEnumTypes = listOf(fileSpec))
    }

    private fun getPackageName(): String {
        return config.packageNameTypes
    }
}
