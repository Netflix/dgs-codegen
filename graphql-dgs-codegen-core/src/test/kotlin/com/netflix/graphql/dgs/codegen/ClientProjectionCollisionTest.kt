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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ClientProjectionCollisionTest {
    @ParameterizedTest(name = "{0}, {1}")
    @MethodSource("codegenCases")
    fun `preserves query projection when operation fields generate the same class name`(
        language: Language,
        schemaOrder: String,
        schema: String,
    ) {
        val codeGenResult =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = "com.netflix.test",
                    language = language,
                    generateClientApi = true,
                    addGeneratedAnnotation = false,
                ),
            ).generate()

        val resultProjection =
            codeGenResult.clientProjections.single { it.typeSpec().name() == "ResultProjectionRoot" }.typeSpec()
        assertThat(resultProjection.methodSpecs())
            .extracting("name")
            .contains("queryOnly")
            .doesNotContain("mutationOnly")

        assertCompilesJava(codeGenResult)
    }

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
            """.trimIndent()

        private val mutationFirstSchema =
            """
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
                Arguments.of(Language.JAVA, "mutation first", mutationFirstSchema),
                Arguments.of(Language.KOTLIN, "query first", queryFirstSchema),
                Arguments.of(Language.KOTLIN, "mutation first", mutationFirstSchema),
            )
    }
}
