/*
 * Copyright 2026 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.graphql.dgs.codegen

import com.squareup.kotlinpoet.TypeSpec
import graphql.language.Field
import graphql.language.OperationDefinition
import graphql.parser.Parser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.net.URLClassLoader

class Kotlin2ClientFilteringTest {
    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `configured filters keep nested projection names stable as fields are added`(extraField: Boolean) {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            "type Query { search: String ${if (extraField) "other: String" else ""} } type Mutation { refresh: Query }",
                        ),
                    packageName = BASE_PACKAGE_NAME,
                    language = Language.KOTLIN,
                    generateClientApi = true,
                    generateKotlinNullableClasses = true,
                    generateKotlinClosureProjections = true,
                    includeQueries = setOf("search"),
                ),
            ).generate()

        assertThat(result.kotlinClientTypes.map { it.name }).contains("QueryNestedProjection")
        assertCompilesKotlin(
            result,
            mapOf(
                "Queries.kt" to
                    """
                    package $BASE_PACKAGE_NAME
                    import $BASE_PACKAGE_NAME.client.QueryNestedProjection
                    fun QueryNestedProjection.common(): QueryNestedProjection = search
                    fun mutation() = DgsClient.buildMutation { refresh { common() } }
                    """.trimIndent(),
            ),
        )
    }

    @Test
    fun `unfiltered query projections retain shared helpers`() {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf("type Query { search: String } type Mutation { refresh: Query }"),
                    packageName = BASE_PACKAGE_NAME,
                    language = Language.KOTLIN,
                    generateClientApi = true,
                    generateKotlinNullableClasses = true,
                    generateKotlinClosureProjections = true,
                ),
            ).generate()

        assertCompilesKotlin(
            result,
            mapOf(
                "Queries.kt" to
                    """
                    package $BASE_PACKAGE_NAME
                    import $BASE_PACKAGE_NAME.client.QueryProjection
                    fun QueryProjection.common(): QueryProjection = search
                    fun query() = DgsClient.buildQuery { common() }
                    fun mutation() = DgsClient.buildMutation { refresh { common() } }
                    """.trimIndent(),
            ),
        )
    }

    @Test
    fun `nested root projections retain inputs and fragments without colliding with schema types`() {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            """
                            schema { query: Root mutation: Mutation }
                            interface Node { selected: String }
                            type Root implements Node {
                                selected: String
                                excluded(input: NestedInput): String
                            }
                            type RootNested { value: String }
                            type Other { value: String }
                            union Payload = Root | Other
                            type Mutation { refresh: [Root!] node: Node payload: Payload }
                            input NestedInput { mode: Mode }
                            enum Mode { ACTIVE }
                            """.trimIndent(),
                        ),
                    packageName = BASE_PACKAGE_NAME,
                    language = Language.KOTLIN,
                    generateClientApi = true,
                    generateDataTypes = false,
                    generateKotlinNullableClasses = true,
                    generateKotlinClosureProjections = true,
                    includeQueries = setOf("selected"),
                ),
            ).generate()

        assertThat(result.kotlinInputTypes.map { it.name }).containsExactly("NestedInput")
        assertThat(result.kotlinEnumTypes.map { it.name }).containsExactly("Mode")
        assertThat(result.kotlinClientTypes.map { it.name }).contains("RootNestedProjection", "RootNested2Projection")
        assertCompilesKotlin(
            result,
            mapOf(
                "Queries.kt" to
                    """
                    package $BASE_PACKAGE_NAME
                    fun field() = DgsClient.buildMutation { refresh { excluded() } }
                    fun interfaceFragment() = DgsClient.buildMutation { node { onRoot { excluded() } } }
                    fun unionFragment() = DgsClient.buildMutation { payload { onRoot { excluded() } } }
                    """.trimIndent(),
            ),
        )
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `nested subscription roots retain typename defaults`(filtered: Boolean) {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            """
                            schema { query: Root subscription: Stream }
                            type Root { stream: Stream }
                            type Stream { event: String other: String }
                            """.trimIndent(),
                        ),
                    packageName = BASE_PACKAGE_NAME,
                    language = Language.KOTLIN,
                    generateClientApi = true,
                    generateKotlinNullableClasses = true,
                    generateKotlinClosureProjections = true,
                    includeSubscriptions = if (filtered) setOf("event") else emptySet(),
                ),
            ).generate()
        val buildDir =
            assertCompilesKotlin(
                result,
                mapOf(
                    "Queries.kt" to
                        """
                        package $BASE_PACKAGE_NAME
                        fun query() = DgsClient.buildRoot { stream { other } }
                        fun subscription() = DgsClient.buildStream { event }
                        """.trimIndent(),
                ),
            )

        URLClassLoader(arrayOf(buildDir.toUri().toURL()), javaClass.classLoader).use { loader ->
            val queries = loader.loadClass("$BASE_PACKAGE_NAME.QueriesKt")
            val query = queries.getMethod("query").invoke(null) as String
            val stream =
                Parser()
                    .parseDocument(query)
                    .getDefinitionsOfType(OperationDefinition::class.java)
                    .single()
                    .selectionSet.selections
                    .filterIsInstance<Field>()
                    .single { it.name == "stream" }
            assertThat(
                stream.selectionSet.selections
                    .filterIsInstance<Field>()
                    .map { it.name },
            ).containsExactly("__typename", "other")
            assertThat(queries.getMethod("subscription").invoke(null) as String).doesNotContain("__typename")
        }
    }

    @Test
    fun `include options retain query fields in mutation payloads`() {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf("type Query { a: String b: String } type Mutation { refresh: Query }"),
                    packageName = BASE_PACKAGE_NAME,
                    language = Language.KOTLIN,
                    generateClientApi = true,
                    generateKotlinNullableClasses = true,
                    generateKotlinClosureProjections = true,
                    includeQueries = setOf("a"),
                ),
            ).generate()

        assertCompilesKotlin(
            result,
            mapOf(
                "Queries.kt" to
                    """
                    package $BASE_PACKAGE_NAME
                    fun mutation() = DgsClient.buildMutation { refresh { b } }
                    """.trimIndent(),
            ),
        )
    }

    @ParameterizedTest
    @CsvSource("Query,timelogs", "Mutation,createTimelog", "Subscription,timelogUpdated")
    fun `nullable root data types retain schema fields`(
        root: String,
        included: String,
    ) {
        val result = generate(root, included)
        val type =
            result.kotlinDataTypes
                .single { it.name == root }
                .members
                .single() as TypeSpec

        assertThat(type.propertySpecs.map { it.name }).contains(included, "other")
    }

    @ParameterizedTest
    @CsvSource(
        "Query,timelogs,true",
        "Query,timelogs,false",
        "Mutation,createTimelog,true",
        "Mutation,createTimelog,false",
        "Subscription,timelogUpdated,true",
        "Subscription,timelogUpdated,false",
    )
    fun `include options restrict root projection fields`(
        root: String,
        included: String,
        generateDataTypes: Boolean,
    ) {
        val result = generate(root, included, generateDataTypes = generateDataTypes)
        val type =
            result.kotlinClientTypes
                .single { it.name == "${root}Projection" }
                .members
                .single() as TypeSpec

        assertThat(type.propertySpecs.map { it.name }).containsExactly(included)
    }

    @ParameterizedTest
    @CsvSource("Query,timelogs", "Mutation,createTimelog", "Subscription,timelogUpdated")
    fun `legacy client respects include options`(
        root: String,
        included: String,
    ) {
        val result = generate(root, included, closureProjections = false)

        assertThat(result.javaQueryTypes.map { it.typeSpec().name() })
            .containsExactly(included.replaceFirstChar(Char::uppercaseChar) + "GraphQLQuery")
    }

    @ParameterizedTest
    @ValueSource(strings = ["Query", "Mutation", "Subscription"])
    fun `include options filter extension fields and methods`(root: String) {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            """
                            type $root { other: Result }
                            extend type $root {
                                selected(id: ID): Result
                                excluded(id: ID): Result
                            }
                            type Result { selected: String other: String }
                            """.trimIndent(),
                        ),
                    language = Language.KOTLIN,
                    generateClientApi = true,
                    generateKotlinNullableClasses = true,
                    generateKotlinClosureProjections = true,
                    includeQueries = setOf("selected"),
                    includeMutations = setOf("selected"),
                    includeSubscriptions = setOf("selected"),
                ),
            ).generate()
        val projection =
            result.kotlinClientTypes
                .single { it.name == "${root}Projection" }
                .members
                .single() as TypeSpec
        val nestedProjection =
            result.kotlinClientTypes
                .single { it.name == "ResultProjection" }
                .members
                .single() as TypeSpec

        assertThat(projection.funSpecs.map { it.name }).containsExactly("selected")
        assertThat(projection.propertySpecs).isEmpty()
        assertThat(nestedProjection.propertySpecs.map { it.name }).containsExactly("selected", "other")
    }

    @ParameterizedTest
    @ValueSource(strings = ["Query", "Mutation", "Subscription"])
    fun `empty include options retain all root projection fields`(root: String) {
        val result = generate(root, "selected", includedFields = emptySet())
        val type =
            result.kotlinClientTypes
                .single { it.name == "${root}Projection" }
                .members
                .single() as TypeSpec

        assertThat(type.propertySpecs.map { it.name }).containsExactly("selected", "other")
    }

    private fun generate(
        root: String,
        included: String,
        generateDataTypes: Boolean = true,
        closureProjections: Boolean = true,
        includedFields: Set<String> = setOf(included),
    ): CodeGenResult =
        CodeGen(
            CodeGenConfig(
                schemas = setOf("type $root { $included: String other: String }"),
                language = Language.KOTLIN,
                generateClientApi = true,
                generateDataTypes = generateDataTypes,
                generateKotlinNullableClasses = true,
                generateKotlinClosureProjections = closureProjections,
                includeQueries = if (root == "Query") includedFields else emptySet(),
                includeMutations = if (root == "Mutation") includedFields else emptySet(),
                includeSubscriptions = if (root == "Subscription") includedFields else emptySet(),
            ),
        ).generate()
}
