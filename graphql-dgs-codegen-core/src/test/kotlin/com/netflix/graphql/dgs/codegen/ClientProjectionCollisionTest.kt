/*
 *
 *  Copyright 2026 Netflix, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ClientProjectionCollisionTest {
    @ParameterizedTest(name = "{0}, {1}")
    @MethodSource("codegenCases")
    fun `generates a root projection per operation when same-named fields return different types`(
        language: Language,
        schemaOrder: String,
        schema: String,
    ) {
        val codeGenResult = generate(schema, language)

        assertThat(rootProjectionMethods(codeGenResult, "ResultGraphQLQueryProjectionRoot"))
            .contains("queryOnly")
            .doesNotContain("mutationOnly", "subscriptionOnly")
        assertThat(rootProjectionMethods(codeGenResult, "ResultGraphQLMutationProjectionRoot"))
            .contains("mutationOnly")
            .doesNotContain("queryOnly", "subscriptionOnly")
        assertThat(rootProjectionMethods(codeGenResult, "ResultProjectionRoot"))
            .contains("subscriptionOnly")
            .doesNotContain("queryOnly", "mutationOnly")

        assertCompilesJava(codeGenResult)
    }

    // Up to 8.7.0 the last operation's ResultProjectionRoot overwrote the others on disk; clients compiled against it
    @Test
    fun `keeps the unqualified root projection for the last operation`() {
        val codeGenResult =
            generate(
                """
                type Query {
                    result: QueryResult
                }

                type Mutation {
                    result: MutationResult
                }

                type QueryResult {
                    queryOnly: String
                }

                type MutationResult {
                    mutationOnly: String
                }
                """.trimIndent(),
                Language.JAVA,
            )

        assertThat(rootProjectionMethods(codeGenResult, "ResultProjectionRoot"))
            .contains("mutationOnly")
            .doesNotContain("queryOnly")
        assertThat(rootProjectionMethods(codeGenResult, "ResultGraphQLQueryProjectionRoot"))
            .contains("queryOnly")
            .doesNotContain("mutationOnly")
    }

    // Up to 8.7.0 the Subscription copy of the Query projection was dropped as a duplicate, so the Mutation one won
    @Test
    fun `shares one root projection when same-named fields return the same type`() {
        val codeGenResult =
            generate(
                """
                type Query {
                    result: Result
                }

                type Mutation {
                    result: OtherResult
                }

                type Subscription {
                    result: Result
                }

                type Result {
                    value: String
                }

                type OtherResult {
                    other: String
                }
                """.trimIndent(),
                Language.JAVA,
            )

        assertThat(codeGenResult.clientProjections.map { it.typeSpec().name() })
            .containsOnlyOnce("ResultProjectionRoot", "ResultGraphQLQueryProjectionRoot", "ResultGraphQLSubscriptionProjectionRoot")
            .doesNotContain("ResultGraphQLMutationProjectionRoot")
        assertThat(rootProjectionMethods(codeGenResult, "ResultProjectionRoot")).contains("other")
        assertThat(rootProjectionMethods(codeGenResult, "ResultGraphQLQueryProjectionRoot")).contains("value")
    }

    private fun generate(
        schema: String,
        language: Language,
    ): CodeGenResult =
        CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = "com.netflix.test",
                language = language,
                generateClientApi = true,
                addGeneratedAnnotation = false,
            ),
        ).generate()

    // single() also fails if two operations emit the same class name, which is how the collision used to surface
    private fun rootProjectionMethods(
        codeGenResult: CodeGenResult,
        className: String,
    ): List<String> =
        codeGenResult.clientProjections
            .single { it.typeSpec().name() == className }
            .typeSpec()
            .methodSpecs()
            .map { it.name() }

    companion object {
        private val queryFirstSchema =
            """
            type Query {
                result: QueryResult
            }

            type QueryResult {
                queryOnly: String
            }

            type Mutation {
                result: MutationResult
            }

            type MutationResult {
                mutationOnly: String
            }

            type Subscription {
                result: SubscriptionResult
            }

            type SubscriptionResult {
                subscriptionOnly: String
            }
            """.trimIndent()

        private val subscriptionFirstSchema =
            """
            type Subscription {
                result: SubscriptionResult
            }

            type SubscriptionResult {
                subscriptionOnly: String
            }

            type Mutation {
                result: MutationResult
            }

            type MutationResult {
                mutationOnly: String
            }

            type Query {
                result: QueryResult
            }

            type QueryResult {
                queryOnly: String
            }
            """.trimIndent()

        @JvmStatic
        fun codegenCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of(Language.JAVA, "query first", queryFirstSchema),
                Arguments.of(Language.JAVA, "subscription first", subscriptionFirstSchema),
                Arguments.of(Language.KOTLIN, "query first", queryFirstSchema),
                Arguments.of(Language.KOTLIN, "subscription first", subscriptionFirstSchema),
            )
    }
}
