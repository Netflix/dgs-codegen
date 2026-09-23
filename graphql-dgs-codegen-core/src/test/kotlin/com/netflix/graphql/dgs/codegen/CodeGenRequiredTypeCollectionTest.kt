/*
 *
 *  Copyright 2026 Netflix, Inc.
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

package com.netflix.graphql.dgs.codegen

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CodeGenRequiredTypeCollectionTest {
    private val schema =
        """
        type Query {
            selected(input: RootInput): SearchResult
        }

        type Mutation {
            selected(input: RootInput): SearchResult
        }

        type Subscription {
            selected(input: RootInput): SearchResult
        }

        input RootInput {
            nested: NestedInput
            kind: RootKind
        }

        input NestedInput {
            value: String
        }

        enum RootKind {
            FIRST
            SECOND
        }

        union SearchResult = Movie | Show

        type Movie {
            title(filter: MovieFilter): String
        }

        input MovieFilter {
            kind: MovieKind
        }

        enum MovieKind {
            FEATURE
            SHORT
        }

        type Show {
            category: ShowCategory
        }

        enum ShowCategory {
            SERIES
            SPECIAL
        }
        """.trimIndent()

    @Test
    fun `full data type generation does not collect required types`() {
        val configurations =
            listOf(
                Language.JAVA to false,
                Language.KOTLIN to false,
                Language.KOTLIN to true,
            )

        configurations.forEach { (language, generateKotlinNullableClasses) ->
            var collectionCount = 0
            val config =
                CodeGenConfig(
                    schemas = setOf(schema),
                    language = language,
                    generateDataTypes = true,
                    generateKotlinNullableClasses = generateKotlinNullableClasses,
                )

            CodeGen(config) { _, _ ->
                collectionCount++
                emptySet()
            }.generate()

            assertThat(collectionCount)
                .describedAs("%s nullable=%s", language, generateKotlinNullableClasses)
                .isZero()
        }
    }

    @Test
    fun `selective generation collects required inputs and enums once`() {
        val configurations =
            listOf(
                Language.JAVA to false,
                Language.KOTLIN to false,
                Language.KOTLIN to true,
            )
        val operationIncludes =
            listOf<(CodeGenConfig) -> Unit>(
                { it.includeQueries = setOf("selected") },
                { it.includeMutations = setOf("selected") },
                { it.includeSubscriptions = setOf("selected") },
            )
        configurations.forEach { (language, generateKotlinNullableClasses) ->
            operationIncludes.forEachIndexed { operationIndex, includeOperation ->
                var collectionCount = 0
                val config =
                    CodeGenConfig(
                        schemas = setOf(schema),
                        language = language,
                        generateDataTypes = false,
                        generateKotlinNullableClasses = generateKotlinNullableClasses,
                    ).also(includeOperation)

                val result =
                    CodeGen(config) { schemaIndex, collectorConfig ->
                        collectionCount++
                        RequiredTypeCollector(schemaIndex, collectorConfig).requiredTypes
                    }.generate()

                assertThat(collectionCount)
                    .describedAs("%s nullable=%s operation %s", language, generateKotlinNullableClasses, operationIndex)
                    .isOne()
                when (language) {
                    Language.JAVA -> {
                        assertThat(result.javaDataTypes)
                            .extracting<String> { it.typeSpec().name() }
                            .containsExactlyInAnyOrder("RootInput", "NestedInput", "MovieFilter")
                        assertThat(result.javaEnumTypes)
                            .extracting<String> { it.typeSpec().name() }
                            .containsExactlyInAnyOrder("RootKind", "MovieKind", "ShowCategory")
                    }

                    Language.KOTLIN -> {
                        val inputTypes =
                            if (generateKotlinNullableClasses) {
                                result.kotlinInputTypes
                            } else {
                                result.kotlinDataTypes
                            }
                        assertThat(inputTypes)
                            .extracting<String> { it.name }
                            .containsExactlyInAnyOrder("RootInput", "NestedInput", "MovieFilter")
                        assertThat(result.kotlinEnumTypes)
                            .extracting<String> { it.name }
                            .containsExactlyInAnyOrder("RootKind", "MovieKind", "ShowCategory")
                    }
                }
            }
        }
    }
}
