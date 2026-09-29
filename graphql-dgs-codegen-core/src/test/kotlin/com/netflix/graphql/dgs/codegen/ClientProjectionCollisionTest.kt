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
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
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

    @ParameterizedTest(name = "{0}, {1}")
    @MethodSource("sameOperationCases")
    fun `first field keeps the 8_7_0 root projection name within one operation`(
        language: Language,
        caseName: String,
        schema: String,
        expectedMethod: String,
        displacedMethod: String,
    ) {
        // Check both the retained legacy root and the additive root in memory; the disk tests pin overwrite order.
        val result = generate(schema, language)

        assertThat(rootProjectionMethods(result, "ResultProjectionRoot"))
            .contains(expectedMethod)
            .doesNotContain(displacedMethod)
        val operation = caseName.substringBefore(',')
        assertThat(rootProjectionMethods(result, "ResultGraphQL${operation}ProjectionRoot"))
            .contains(displacedMethod)
            .doesNotContain(expectedMethod)
        // Subscription's generated data type has duplicate getResult() methods for these two schema fields.
        if (operation == "Mutation") assertCompilesJava(result)
    }

    @ParameterizedTest(name = "{0}, {1}")
    @MethodSource("sameOperationQueryOnDiskCases")
    fun `Query writes the first root and last query class when field names capitalize alike`(
        language: Language,
        caseName: String,
        schema: String,
        expectedMethod: String,
        displacedMethod: String,
        @TempDir outputDir: Path,
    ) {
        generateToDisk(schema, language, outputDir)

        assertThat(outputDir.resolve("com/netflix/test/client/ResultProjectionRoot.java"))
            .content()
            .contains("$expectedMethod()")
            .doesNotContain("$displacedMethod()")
        val lastField = if (expectedMethod == "aOnly") "Result" else "result"
        assertThat(outputDir.resolve("com/netflix/test/client/ResultGraphQLQuery.java"))
            .content()
            .contains("return \"$lastField\";")
    }

    @ParameterizedTest
    @EnumSource(Language::class)
    fun `Mutation duplicate query classes keep the last field while its first root wins`(
        language: Language,
        @TempDir outputDir: Path,
    ) {
        generateToDisk(
            """
            type Query { result: A }
            type Mutation { result: B, Result: C }
            type A { aOnly: String }
            type B { bOnly: String }
            type C { cOnly: String }
            """.trimIndent(),
            language,
            outputDir,
        )

        assertThat(outputDir.resolve("com/netflix/test/client/ResultProjectionRoot.java"))
            .content()
            .contains("bOnly()")
            .doesNotContain("aOnly()", "cOnly()")
        assertThat(outputDir.resolve("com/netflix/test/client/ResultGraphQLMutation.java"))
            .content()
            .contains("return \"Result\";")
    }

    @ParameterizedTest
    @EnumSource(Language::class)
    fun `operation extensions preserve the last definition's legacy root`(
        language: Language,
        @TempDir outputDir: Path,
    ) {
        for (operation in listOf("Query", "Mutation")) {
            generateToDisk(
                """
                type $operation { result: A }
                extend type $operation { Result: B }
                type A { aOnly: String }
                type B { bOnly: String }
                """.trimIndent(),
                language,
                outputDir.resolve(operation),
            )

            val clientDir = outputDir.resolve("$operation/com/netflix/test/client")
            assertThat(clientDir.resolve("ResultProjectionRoot.java"))
                .content()
                .contains("bOnly()")
                .doesNotContain("aOnly()")
            assertThat(clientDir.resolve("ResultGraphQL${operation}ProjectionRoot.java"))
                .content()
                .contains("aOnly()")
                .doesNotContain("bOnly()")
        }
    }

    // Mutation and Subscription use distinct query-class names here, while Query's duplicate path is checked above.
    @ParameterizedTest(name = "{0}, {1}")
    @MethodSource("sameOperationOnDiskCases")
    fun `writes the first same-named root within Mutation and Subscription`(
        language: Language,
        caseName: String,
        schema: String,
        expectedMethod: String,
        displacedMethod: String,
        @TempDir outputDir: Path,
    ) {
        generateToDisk(schema, language, outputDir)

        assertThat(outputDir.resolve("com/netflix/test/client/ResultProjectionRoot.java"))
            .content()
            .contains("$expectedMethod()")
            .doesNotContain("$displacedMethod()")
    }

    // 8.7.0 never generated a qualified root, so this schema wrote both field roots even though the writer now
    // rejects paths that differ only by case
    @ParameterizedTest
    @EnumSource(Language::class)
    fun `qualified root names avoid case-only clashes with field projection names`(
        language: Language,
        @TempDir outputDir: Path,
    ) {
        generateToDisk(
            """
            type Query {
                result: A
                resultgraphQLQuery: C
            }

            type Mutation {
                result: B
            }

            type A {
                aOnly: String
            }

            type B {
                bOnly: String
            }

            type C {
                cOnly: String
            }
            """.trimIndent(),
            language,
            outputDir,
        )

        assertThat(outputDir.resolve("com/netflix/test/client/ResultProjectionRoot.java")).content().contains("bOnly()")
        assertThat(outputDir.resolve("com/netflix/test/client/ResultgraphQLQueryProjectionRoot.java"))
            .content()
            .contains("cOnly()")
    }

    @ParameterizedTest
    @EnumSource(Language::class)
    fun `qualified root names avoid existing field projection names`(language: Language) {
        val result =
            generate(
                """
                type Query {
                    result: QueryResult
                    resultGraphQLQuery: OtherQueryResult
                }

                type Mutation {
                    result: MutationResult
                }

                type QueryResult {
                    queryOnly: String
                }

                type OtherQueryResult {
                    otherQueryOnly: String
                }

                type MutationResult {
                    mutationOnly: String
                }
                """.trimIndent(),
                language,
            )

        assertThat(rootProjectionMethods(result, "ResultProjectionRoot")).contains("mutationOnly")
        assertThat(rootProjectionMethods(result, "ResultGraphQLQueryProjectionRoot")).contains("otherQueryOnly")
        assertThat(rootProjectionMethods(result, "ResultGraphQLQuery2ProjectionRoot")).contains("queryOnly")
        assertCompilesJava(result)
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

    // Up to 8.7.0 the federation root was written after the client one and won on disk
    @Test
    fun `keeps EntitiesProjectionRoot for federated entities when a query field is named entities`() {
        val codeGenResult =
            generate(
                """
                type Query {
                    entities: [Movie]
                }

                type Movie @key(fields: "id") {
                    id: ID
                    title: String
                }
                """.trimIndent(),
                Language.JAVA,
            )

        assertThat(rootProjectionMethods(codeGenResult, "EntitiesProjectionRoot")).contains("onMovie")
        assertThat(rootProjectionMethods(codeGenResult, "EntitiesGraphQLQueryProjectionRoot"))
            .contains("title")
            .doesNotContain("onMovie")
    }

    @Test
    fun `federation root stays reserved when a query type is named _entities`() {
        val result =
            generate(
                """
                type Query {
                    entities: _entities
                }

                type _entities {
                    custom: String
                }

                type Movie @key(fields: "id") {
                    id: ID
                }
                """.trimIndent(),
                Language.JAVA,
            )

        assertThat(rootProjectionMethods(result, "EntitiesProjectionRoot")).contains("onMovie")
        assertThat(rootProjectionMethods(result, "EntitiesGraphQLQueryProjectionRoot")).contains("custom")
        assertCompilesJava(result)
    }

    @ParameterizedTest(name = "{0}, {1}")
    @MethodSource("subprojectionWinnerCases")
    fun `writes the 8_7_0 winner for same-named subprojections`(
        language: Language,
        caseName: String,
        schema: String,
        expectedMethod: String,
        displacedMethod: String,
        @TempDir outputDir: Path,
    ) {
        generateToDisk(schema, language, outputDir)

        assertThat(outputDir.resolve("com/netflix/test/client/MovieFragmentProjection.java"))
            .content()
            .contains("$expectedMethod()")
            .doesNotContain("$displacedMethod()")
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

    private fun generateToDisk(
        schema: String,
        language: Language,
        outputDir: Path,
    ) {
        CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = "com.netflix.test",
                language = language,
                generateClientApi = true,
                addGeneratedAnnotation = false,
                writeToFiles = true,
                outputDir = outputDir,
                examplesOutputDir = outputDir.resolve("examples"),
                generatedDocsFolder = outputDir.resolve("docs"),
            ),
        ).generate()
    }

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
        private const val COLLISION_TYPES =
            """
            union SearchResult = Movie | Show
            type Movie { title: String }
            type Show { name: String }
            type Wrapper { movieFragment: MovieFragment }
            type MovieFragment { fragmentOnly: String }
            """

        private const val LOSER_TYPES = "type A { aOnly: String } type B { movieFragment: MovieFragment }"

        @JvmStatic
        fun sameOperationCases(): Stream<Arguments> =
            Stream.of("Query", "Mutation", "Subscription").flatMap { operation ->
                Stream
                    .of(
                        Arguments.of("lowercase first", "result: A, Result: B", "aOnly", "bOnly"),
                        Arguments.of("uppercase first", "Result: B, result: A", "bOnly", "aOnly"),
                    ).flatMap { case ->
                        val values = case.get()
                        Language.entries.stream().map { language ->
                            Arguments.of(
                                language,
                                "$operation, ${values[0]}",
                                "type $operation { ${values[1]} } type A { aOnly: String } type B { bOnly: String }",
                                values[2],
                                values[3],
                            )
                        }
                    }
            }

        @JvmStatic
        fun sameOperationOnDiskCases(): Stream<Arguments> = sameOperationCases().filter { !(it.get()[1] as String).startsWith("Query") }

        @JvmStatic
        fun sameOperationQueryOnDiskCases(): Stream<Arguments> = sameOperationCases().filter { (it.get()[1] as String).startsWith("Query") }

        @JvmStatic
        fun subprojectionWinnerCases(): Stream<Arguments> =
            Stream
                .of(
                    Arguments.of(
                        "union fragment then object type",
                        "type Query { search: SearchResult } type Mutation { wrap: Wrapper }",
                        "fragmentOnly",
                        "title",
                    ),
                    Arguments.of(
                        "object type then union fragment",
                        "type Query { wrap: Wrapper } type Mutation { search: SearchResult }",
                        "title",
                        "fragmentOnly",
                    ),
                    Arguments.of(
                        "later operation reuses fragment before colliding type",
                        "type Query { search: SearchResult } type Mutation { find: SearchResult, wrap: Wrapper }",
                        "title",
                        "fragmentOnly",
                    ),
                    Arguments.of(
                        "later operation reuses type before colliding fragment",
                        "type Query { wrap: Wrapper } type Mutation { wrapAgain: Wrapper, search: SearchResult }",
                        "fragmentOnly",
                        "title",
                    ),
                    // the type is skipped in Query's Wrapper by the name clash, so Mutation is the first to build it
                    Arguments.of(
                        "later operation reaches a type whose descendant lost the name earlier",
                        "type Query { search: SearchResult, wrap: Wrapper } type Mutation { wrap: Wrapper, search: SearchResult }",
                        "fragmentOnly",
                        "title",
                    ),
                    // 8.7.0 skipped the displaced Result root without walking it, so B never claimed the name
                    Arguments.of(
                        "same-operation loser reaches a type after an earlier operation's fragment",
                        "type Query { search: SearchResult } type Mutation { result: A, Result: B } $LOSER_TYPES",
                        "title",
                        "fragmentOnly",
                    ),
                    Arguments.of(
                        "same-operation loser reaches a type before a later field's fragment",
                        "type Mutation { result: A, Result: B, search: SearchResult } $LOSER_TYPES",
                        "title",
                        "fragmentOnly",
                    ),
                    // the entities generator rebuilds Query's type projection last; only merge dedupe keeps the fragment
                    Arguments.of(
                        "entities rebuild a type after a later operation's fragment",
                        "type Query { wrap: Wrapper } type Mutation { search: SearchResult } " +
                            "type Film @key(fields: \"id\") { id: ID, movieFragment: MovieFragment }",
                        "title",
                        "fragmentOnly",
                    ),
                    Arguments.of(
                        "one operation, fragment then type",
                        "type Query { search: SearchResult, wrap: Wrapper }",
                        "title",
                        "fragmentOnly",
                    ),
                    Arguments.of(
                        "one operation, type then fragment",
                        "type Query { wrap: Wrapper, search: SearchResult }",
                        "fragmentOnly",
                        "title",
                    ),
                ).flatMap { scenario ->
                    val values = scenario.get()
                    Language.entries.stream().map { language ->
                        Arguments.of(language, values[0], "${values[1]}\n$COLLISION_TYPES", values[2], values[3])
                    }
                }

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
