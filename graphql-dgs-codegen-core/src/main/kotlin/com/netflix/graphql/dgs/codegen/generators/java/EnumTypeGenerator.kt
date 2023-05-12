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
import com.netflix.graphql.dgs.codegen.generators.shared.SiteTarget
import com.netflix.graphql.dgs.codegen.generators.shared.applyDirectivesJava
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import graphql.language.EnumTypeDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.lang.model.element.Modifier

class EnumTypeGenerator(private val config: CodeGenConfig) {
    private val logger: Logger = LoggerFactory.getLogger(EnumTypeGenerator::class.java)

    fun generate(definition: EnumTypeDefinition, extensions: List<EnumTypeDefinition>): CodeGenResult {
        if (definition.shouldSkip(config)) {
            return CodeGenResult()
        }

        logger.info("Generating enum type ${definition.name}")

        val javaType =
            TypeSpec
                .enumBuilder(definition.name)
                .addModifiers(Modifier.PUBLIC)
                .addOptionalGeneratedAnnotation(config)

        if (definition.description != null) {
            javaType.addJavadoc(definition.description.sanitizeJavaDoc())
        }

        val mergedEnumDefinitions = definition.enumValueDefinitions + extensions.flatMap { it.enumValueDefinitions }

        mergedEnumDefinitions.forEach {
            javaType.addEnumConstant(it.name)
            if (it.directives.isNotEmpty()) {
                val (annotations, comments) = applyDirectivesJava(it.directives, config)
                if (!comments.isNullOrBlank()) {
                    javaType.addJavadoc("\$L", comments)
                }
                for (entry in annotations) {
                    when (SiteTarget.valueOf(entry.key)) {
                        SiteTarget.FIELD -> javaType.addAnnotations(annotations[SiteTarget.FIELD.name])
                        else -> javaType.addAnnotations(annotations[entry.key])
                    }
                }
            }
        }

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()

        return CodeGenResult(javaEnumTypes = listOf(javaFile))
    }

    private fun getPackageName(): String {
        return config.packageNameTypes
    }
}
