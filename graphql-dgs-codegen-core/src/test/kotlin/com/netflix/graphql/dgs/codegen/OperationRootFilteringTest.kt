/*
 * Copyright 2026 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.graphql.dgs.codegen

import com.squareup.kotlinpoet.TypeSpec
import graphql.language.ObjectTypeDefinition
import graphql.parser.Parser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.net.URLClassLoader
import java.util.Locale

class OperationRootFilteringTest {
    @ParameterizedTest
    @CsvSource("JAVA,false", "KOTLIN,false", "KOTLIN,true")
    fun `type only generation collects inputs only for explicitly included operations`(
        language: Language,
        closureProjections: Boolean,
    ) {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            """
                            type Query {
                                search(input: SearchInput): String
                                other(input: OtherInput): String
                            }
                            type Mutation { refresh(input: UpdateInput): Query }
                            type Subscription { watch(mode: WatchMode): String }
                            input SearchInput { value: String }
                            input OtherInput { value: String }
                            input UpdateInput { value: String }
                            enum WatchMode { ACTIVE }
                            """.trimIndent(),
                        ),
                    language = language,
                    generateClientApi = false,
                    generateDataTypes = false,
                    generateKotlinNullableClasses = closureProjections,
                    generateKotlinClosureProjections = closureProjections,
                    includeQueries = setOf("search"),
                ),
            ).generate()

        when {
            language == Language.JAVA -> {
                assertThat(result.javaDataTypes.map { it.typeSpec().name() }).containsExactly("SearchInput")
                assertThat(result.javaEnumTypes).isEmpty()
            }

            closureProjections -> {
                assertThat(result.kotlinInputTypes.map { it.name }).containsExactly("SearchInput")
                assertThat(result.kotlinEnumTypes).isEmpty()
            }

            else -> {
                assertThat(result.kotlinDataTypes.map { it.name }).containsExactly("SearchInput")
                assertThat(result.kotlinEnumTypes).isEmpty()
            }
        }
    }

    @ParameterizedTest
    @CsvSource("JAVA,false", "KOTLIN,true")
    fun `client generation retains inputs for roots with empty include options`(
        language: Language,
        closureProjections: Boolean,
    ) {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            """
                            type Query { search: String other(input: OtherInput): String }
                            type Mutation { update(input: UpdateInput): String }
                            type Subscription { watch(mode: WatchMode): String }
                            input OtherInput { value: String }
                            input UpdateInput { value: String }
                            enum WatchMode { ACTIVE }
                            """.trimIndent(),
                        ),
                    packageName = BASE_PACKAGE_NAME,
                    language = language,
                    generateClientApi = true,
                    generateDataTypes = false,
                    generateKotlinNullableClasses = closureProjections,
                    generateKotlinClosureProjections = closureProjections,
                    includeQueries = setOf("search"),
                ),
            ).generate()

        if (language == Language.JAVA) {
            assertThat(result.javaDataTypes.map { it.typeSpec().name() }).containsExactly("UpdateInput")
            assertThat(result.javaEnumTypes.map { it.typeSpec().name() }).containsExactly("WatchMode")
            assertCompilesJava(result)
        } else {
            assertThat(result.kotlinInputTypes.map { it.name }).containsExactly("UpdateInput")
            assertThat(result.kotlinEnumTypes.map { it.name }).containsExactly("WatchMode")
            assertCompilesKotlin(
                result,
                mapOf(
                    "Queries.kt" to
                        """
                        package $BASE_PACKAGE_NAME
                        fun query() = DgsClient.buildQuery { search }
                        fun mutation() = DgsClient.buildMutation { update() }
                        fun subscription() = DgsClient.buildSubscription { watch() }
                        """.trimIndent(),
                ),
            )
        }
    }

    @ParameterizedTest
    @CsvSource(
        "query,JAVA,false",
        "mutation,JAVA,false",
        "subscription,JAVA,false",
        "query,KOTLIN,false",
        "mutation,KOTLIN,false",
        "subscription,KOTLIN,false",
        "query,KOTLIN,true",
        "mutation,KOTLIN,true",
        "subscription,KOTLIN,true",
    )
    fun `empty include options retain inputs required by renamed roots`(
        operation: String,
        language: Language,
        closureProjections: Boolean,
    ) {
        val rootDeclaration = if (operation == "query") "query: Root" else "query: Query $operation: Root"
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            """
                            schema { $rootDeclaration }
                            type Query { health: String }
                            type Root { selected(input: SelectedInput): String }
                            input SelectedInput { mode: Mode }
                            enum Mode { ACTIVE }
                            """.trimIndent(),
                        ),
                    packageName = BASE_PACKAGE_NAME,
                    language = language,
                    generateClientApi = true,
                    generateDataTypes = false,
                    generateKotlinNullableClasses = closureProjections,
                    generateKotlinClosureProjections = closureProjections,
                ),
            ).generate()

        if (language == Language.JAVA) {
            assertThat(result.javaDataTypes.map { it.typeSpec().name() }).containsExactly("SelectedInput")
            assertThat(result.javaEnumTypes.map { it.typeSpec().name() }).containsExactly("Mode")
            assertCompilesJava(result)
        } else {
            val inputs = if (closureProjections) result.kotlinInputTypes else result.kotlinDataTypes
            assertThat(inputs.map { it.name }).containsExactly("SelectedInput")
            assertThat(result.kotlinEnumTypes.map { it.name }).containsExactly("Mode")
            assertCompilesKotlin(result)
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun `public schema aware filter honors renamed roots and preserves the legacy overload`() {
        val document = Parser().parseDocument("schema { query: Root } type Root { selected: String excluded: String }")
        val fields = document.getDefinitionsOfType(ObjectTypeDefinition::class.java).single().fieldDefinitions
        val config = CodeGenConfig(includeQueries = setOf("selected"))
        assertThat(fields.filterIncludedInConfig("Root", config, document).map { it.name }).containsExactly("selected")
        assertThat(fields.filterIncludedInConfig("Root", config).map { it.name }).containsExactly("selected", "excluded")
    }

    @Test
    fun `shared subscription root keeps query typename defaults`() {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf("schema { query: Root subscription: Root } type Root { a: String }"),
                    packageName = BASE_PACKAGE_NAME,
                    language = Language.KOTLIN,
                    generateClientApi = true,
                    generateKotlinNullableClasses = true,
                    generateKotlinClosureProjections = true,
                ),
            ).generate()
        val buildDir =
            assertCompilesKotlin(
                result,
                mapOf(
                    "Queries.kt" to
                        """
                        package $BASE_PACKAGE_NAME
                        fun query() = DgsClient.buildRoot { a }
                        fun subscription() = DgsClient.buildRootSubscription { a }
                        """.trimIndent(),
                ),
            )
        URLClassLoader(arrayOf(buildDir.toUri().toURL()), javaClass.classLoader).use { loader ->
            val queries = loader.loadClass("$BASE_PACKAGE_NAME.QueriesKt")
            assertThat(queries.getMethod("query").invoke(null) as String).contains("__typename")
            assertThat(queries.getMethod("subscription").invoke(null) as String).doesNotContain("__typename")
        }
    }

    @Test
    fun `shared root builder names preserve other root entry points`() {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            """
                            schema { query: Root mutation: Root subscription: RootMutation }
                            type Root { a: String }
                            type RootMutation { b: String }
                            """.trimIndent(),
                        ),
                    packageName = BASE_PACKAGE_NAME,
                    language = Language.KOTLIN,
                    generateClientApi = true,
                    generateKotlinNullableClasses = true,
                    generateKotlinClosureProjections = true,
                ),
            ).generate()
        val client =
            result.kotlinClientTypes
                .single { it.name == "DgsClient" }
                .members
                .single() as TypeSpec
        assertThat(client.funSpecs.map { it.name }).containsExactly("buildRoot", "buildRootMutation2", "buildRootMutation")
        assertCompilesKotlin(result)
    }

    @ParameterizedTest
    @CsvSource("JAVA,false", "KOTLIN,false", "KOTLIN,true")
    fun `shared roots retain operation filters and required inputs`(
        language: Language,
        closureProjections: Boolean,
    ) {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            """
                            schema { query: Root mutation: Root subscription: Root }
                            type Root {
                                read(input: ReadInput): Item
                                write(input: WriteInput): Item
                                watch(input: WatchInput): Item
                                excluded(input: ExcludedInput): Item
                            }
                            type Item { value: String }
                            input ReadInput { value: String }
                            input WriteInput { value: String }
                            input WatchInput { value: String }
                            input ExcludedInput { value: String }
                            """.trimIndent(),
                        ),
                    packageName = BASE_PACKAGE_NAME,
                    language = language,
                    generateClientApi = true,
                    generateDataTypes = false,
                    generateKotlinNullableClasses = closureProjections,
                    generateKotlinClosureProjections = closureProjections,
                    includeQueries = setOf("read"),
                    includeMutations = setOf("write"),
                    includeSubscriptions = setOf("watch"),
                ),
            ).generate()

        if (closureProjections) {
            val root =
                result.kotlinClientTypes
                    .single { it.name == "RootProjection" }
                    .members
                    .single() as TypeSpec
            assertThat(root.funSpecs.map { it.name }).containsExactly("read", "write", "watch")
            val client =
                result.kotlinClientTypes
                    .single { it.name == "DgsClient" }
                    .members
                    .single() as TypeSpec
            assertThat(client.funSpecs.map { it.name }).containsExactly("buildRoot", "buildRootMutation", "buildRootSubscription")
            assertThat(client.funSpecs.map { it.toString() }).zipSatisfy(listOf("QUERY", "MUTATION", "SUBSCRIPTION")) { source, operation ->
                assertThat(source).contains("Operation.$operation")
            }
            assertThat(root.superclassConstructorParameters.joinToString()).contains("defaultFields = defaultFields")
            assertThat(result.kotlinInputTypes.map { it.name }).containsExactlyInAnyOrder("ReadInput", "WriteInput", "WatchInput")
        } else {
            assertThat(
                result.javaQueryTypes.map { it.typeSpec().name() },
            ).containsExactly("ReadGraphQLQuery", "WriteGraphQLQuery", "WatchGraphQLQuery")
            assertThat(
                result.javaQueryTypes.map { it.toString() },
            ).zipSatisfy(listOf("query", "mutation", "subscription")) { source, operation ->
                assertThat(source).contains("super(\"$operation\", queryName)")
            }
            val inputs =
                if (language ==
                    Language.JAVA
                ) {
                    result.javaDataTypes.map { it.typeSpec().name() }
                } else {
                    result.kotlinDataTypes.map { it.name }
                }
            assertThat(inputs).containsExactlyInAnyOrder("ReadInput", "WriteInput", "WatchInput")
        }
        if (language == Language.JAVA) assertCompilesJava(result) else assertCompilesKotlin(result)
    }

    @ParameterizedTest
    @CsvSource(
        "query,MyQuery,JAVA,false",
        "mutation,MyMutation,JAVA,false",
        "subscription,MySubscription,JAVA,false",
        "query,MyQuery,KOTLIN,false",
        "mutation,MyMutation,KOTLIN,false",
        "subscription,MySubscription,KOTLIN,false",
        "query,MyQuery,KOTLIN,true",
        "mutation,MyMutation,KOTLIN,true",
        "subscription,MySubscription,KOTLIN,true",
    )
    fun `include options follow declared operation roots`(
        operation: String,
        root: String,
        language: Language,
        closureProjections: Boolean,
    ) {
        val rootDeclaration = if (operation == "query") "query: $root" else "query: Query $operation: $root"
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            """
                            schema { $rootDeclaration }
                            type Query { unused: String }
                            type $root {
                                selected(input: SelectedInput): String
                                excluded(input: ExcludedInput): String
                            }
                            input SelectedInput { value: String }
                            input ExcludedInput { value: String }
                            """.trimIndent(),
                        ),
                    language = language,
                    packageName = BASE_PACKAGE_NAME,
                    generateClientApi = true,
                    generateDataTypes = false,
                    generateKotlinNullableClasses = closureProjections,
                    generateKotlinClosureProjections = closureProjections,
                    includeQueries = setOf("selected"),
                    includeMutations = setOf("selected"),
                    includeSubscriptions = setOf("selected"),
                ),
            ).generate()

        if (closureProjections) {
            val projection =
                result.kotlinClientTypes
                    .single { it.name == "${root}Projection" }
                    .members
                    .single() as TypeSpec
            assertThat(projection.funSpecs.map { it.name }).containsExactly("selected")
            assertThat(result.kotlinInputTypes.map { it.name }).containsExactly("SelectedInput")
            val client = result.kotlinClientTypes.single { it.name == "DgsClient" }.toString()
            assertThat(client).contains("build$root(", "Operation.${operation.uppercase(Locale.ROOT)}")
            if (operation == "subscription") {
                assertThat(projection.superclassConstructorParameters.joinToString()).contains("defaultFields = emptySet()")
            }
        } else {
            assertThat(result.javaQueryTypes.map { it.typeSpec().name() }).containsExactly("SelectedGraphQLQuery")
            assertThat(result.javaQueryTypes.single().toString()).contains("super(\"$operation\", queryName)")
            if (language == Language.JAVA) {
                assertThat(result.javaDataTypes.map { it.typeSpec().name() }).containsExactly("SelectedInput")
                assertCompilesJava(result)
            } else {
                assertThat(result.kotlinDataTypes.map { it.name }).containsExactly("SelectedInput")
            }
        }
    }

    @ParameterizedTest
    @CsvSource("JAVA,false", "KOTLIN,false", "KOTLIN,true")
    fun `conventional names are ordinary types when roots are declared explicitly`(
        language: Language,
        closureProjections: Boolean,
    ) {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            """
                            schema { query: Root }
                            type Root { selected: Query excluded: String }
                            type Query { selected: Mutation other: Subscription }
                            type Mutation { value: String other: String }
                            type Subscription { value: String other: String }
                            """.trimIndent(),
                        ),
                    packageName = BASE_PACKAGE_NAME,
                    language = language,
                    generateClientApi = true,
                    generateKotlinNullableClasses = closureProjections,
                    generateKotlinClosureProjections = closureProjections,
                    includeQueries = setOf("selected"),
                    includeMutations = setOf("selected"),
                    includeSubscriptions = setOf("selected"),
                ),
            ).generate()

        if (closureProjections) {
            val query =
                result.kotlinClientTypes
                    .single { it.name == "QueryProjection" }
                    .members
                    .single() as TypeSpec
            val subscription =
                result.kotlinClientTypes
                    .single { it.name == "SubscriptionProjection" }
                    .members
                    .single() as TypeSpec
            assertThat(query.funSpecs.map { it.name }).containsExactly("selected", "other")
            assertThat(subscription.propertySpecs.map { it.name }).containsExactly("value", "other")
            assertThat(subscription.superclassConstructorParameters.joinToString()).doesNotContain("defaultFields = emptySet()")
            val client = result.kotlinClientTypes.single { it.name == "DgsClient" }.toString()
            assertThat(client).contains("buildRoot(").doesNotContain("buildQuery(", "buildMutation(", "buildSubscription(")
            assertCompilesKotlin(result)
        } else {
            assertThat(result.javaQueryTypes.map { it.typeSpec().name() }).containsExactly("SelectedGraphQLQuery")
            if (language == Language.JAVA) {
                assertThat(
                    result.javaDataTypes.map { it.typeSpec().name() },
                ).contains("Query", "Mutation", "Subscription", "Root")
                assertCompilesJava(result)
            } else {
                assertThat(result.kotlinDataTypes.map { it.name }).contains("Query", "Mutation", "Subscription", "Root")
                assertCompilesKotlin(result)
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        "JAVA,false,true",
        "JAVA,false,false",
        "KOTLIN,false,true",
        "KOTLIN,false,false",
        "KOTLIN,true,true",
        "KOTLIN,true,false",
    )
    fun `schema extensions declare filtered operation roots`(
        language: Language,
        closureProjections: Boolean,
        explicitSchema: Boolean,
    ) {
        val schemaDeclaration = if (explicitSchema) "schema { query: Query }" else ""
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            """
                            $schemaDeclaration
                            extend schema { mutation: WriteRoot subscription: StreamRoot }
                            type Query { health: String }
                            type WriteRoot { mutate: String excluded: String }
                            type StreamRoot { observe: String excluded: String }
                            type Mutation { unrelated: String }
                            type Subscription { unrelated: String }
                            """.trimIndent(),
                        ),
                    packageName = BASE_PACKAGE_NAME,
                    language = language,
                    generateClientApi = true,
                    generateKotlinNullableClasses = closureProjections,
                    generateKotlinClosureProjections = closureProjections,
                    includeQueries = setOf(""),
                    includeMutations = setOf("mutate"),
                    includeSubscriptions = setOf("observe"),
                ),
            ).generate()

        if (closureProjections) {
            for ((root, selected) in listOf("WriteRoot" to "mutate", "StreamRoot" to "observe")) {
                val projection =
                    result.kotlinClientTypes
                        .single { it.name == "${root}Projection" }
                        .members
                        .single() as TypeSpec
                assertThat(projection.propertySpecs.map { it.name }).containsExactly(selected)
            }
            val client = result.kotlinClientTypes.single { it.name == "DgsClient" }.toString()
            assertThat(client)
                .contains("buildQuery(", "buildWriteRoot(", "buildStreamRoot(")
                .doesNotContain("buildMutation(", "buildSubscription(")
            assertCompilesKotlin(result)
        } else {
            assertThat(result.javaQueryTypes.map { it.typeSpec().name() }).containsExactly("MutateGraphQLQuery", "ObserveGraphQLQuery")
            assertThat(result.javaQueryTypes[0].toString()).contains("super(\"mutation\", queryName)")
            assertThat(result.javaQueryTypes[1].toString()).contains("super(\"subscription\", queryName)")
            if (language == Language.JAVA) {
                assertCompilesJava(result)
            } else {
                assertCompilesKotlin(result)
            }
        }
    }
}
