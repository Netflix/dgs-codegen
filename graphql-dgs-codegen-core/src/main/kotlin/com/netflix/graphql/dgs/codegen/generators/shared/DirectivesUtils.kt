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

package com.netflix.graphql.dgs.codegen.generators.shared

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.generators.kotlin.customAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.deprecatedAnnotation
import com.squareup.kotlinpoet.AnnotationSpec
import graphql.language.Directive
import graphql.language.StringValue
import graphql.language.Value
import com.squareup.javapoet.AnnotationSpec as JavaAnnotationSpec

fun createArgumentMap(directive: Directive): MutableMap<String, Value<Value<*>>> =
    directive.arguments.fold(mutableMapOf()) { argMap, argument ->
        argMap[argument.name] = argument.value
        argMap
    }

/**
 * Applies directives like customAnnotation
 */
fun applyDirectivesKotlin(
    directives: List<Directive>,
    config: CodeGenConfig,
): MutableList<AnnotationSpec> =
    directives.fold(mutableListOf()) { annotations, directive ->
        val argumentMap = createArgumentMap(directive)
        if (directive.name == ParserConstants.CUSTOM_ANNOTATION && config.generateCustomAnnotations) {
            annotations.add(customAnnotation(argumentMap, config))
        }
        if (directive.name == ParserConstants.DEPRECATED && config.addDeprecatedAnnotation) {
            if (argumentMap.containsKey(ParserConstants.REASON)) {
                annotations.add(deprecatedAnnotation((argumentMap[ParserConstants.REASON] as StringValue).value))
            } else {
                annotations.add(deprecatedAnnotation(ParserConstants.DEPRECATED_IN_THE_GRAPHQL_SCHEMA))
            }
        }

        annotations
    }

/**
 * Applies directives like customAnnotation, deprecated etc. The target value in the directives is used to decide where to apply the annotation.
 * @input directives: list of directive that needs to be applied
 * @input config: code generator config
 * @return Pair of (map of target site and corresponding annotations) and comments
 */
fun applyDirectivesJava(
    directives: List<Directive>,
    config: CodeGenConfig,
): Pair<MutableMap<String, MutableList<JavaAnnotationSpec>>, String?> {
    var commentFormat: String? = null
    return Pair(
        directives.fold(mutableMapOf()) { annotations, directive ->
            val argumentMap = createArgumentMap(directive)
            val siteTarget =
                if (argumentMap.containsKey(
                        ParserConstants.SITE_TARGET,
                    )
                ) {
                    (argumentMap[ParserConstants.SITE_TARGET] as StringValue).value.uppercase()
                } else {
                    SiteTarget.DEFAULT.name
                }
            if (directive.name == ParserConstants.CUSTOM_ANNOTATION && config.generateCustomAnnotations) {
                annotations[siteTarget] =
                    if (annotations.containsKey(siteTarget)) {
                        var annotationList: MutableList<JavaAnnotationSpec> = annotations[siteTarget]!!
                        annotationList.add(
                            com.netflix.graphql.dgs.codegen.generators.java.customAnnotation(
                                argumentMap,
                                config,
                            ),
                        )
                        annotationList
                    } else {
                        mutableListOf(
                            com.netflix.graphql.dgs.codegen.generators.java
                                .customAnnotation(argumentMap, config),
                        )
                    }
            }
            if (directive.name == ParserConstants.DEPRECATED && config.addDeprecatedAnnotation) {
                annotations[siteTarget] = mutableListOf(JavaAnnotationSpec.builder(java.lang.Deprecated::class.java).build())
                if (argumentMap.containsKey(ParserConstants.REASON)) {
                    val reason: String = (argumentMap[ParserConstants.REASON] as StringValue).value
                    val replace = reason.substringAfter(ParserConstants.REPLACE_WITH_STR, "")
                    commentFormat = reason.substringBefore(ParserConstants.REPLACE_WITH_STR)
                    if (replace.isNotEmpty()) {
                        commentFormat = "@deprecated ${reason.substringBefore(ParserConstants.REPLACE_WITH_STR)}. Replaced by $replace"
                    }
                } else {
                    commentFormat = ParserConstants.DEPRECATED_IN_THE_GRAPHQL_SCHEMA
                }
            }
            annotations
        },
        commentFormat,
    )
}
