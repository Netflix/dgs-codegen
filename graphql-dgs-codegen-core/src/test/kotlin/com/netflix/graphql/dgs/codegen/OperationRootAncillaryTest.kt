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
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class OperationRootAncillaryTest {
    @ParameterizedTest
    @CsvSource("JAVA,false", "KOTLIN,false", "KOTLIN,true")
    fun `explicit schema without query entry retains the Query fallback`(
        language: Language,
        closureProjections: Boolean,
    ) {
        val schema = "schema { mutation: M } type Query { a: String } type M { b: String }"
        val executableSchema =
            SchemaGenerator().makeExecutableSchema(
                SchemaParser().parse(schema),
                RuntimeWiring.newRuntimeWiring().build(),
            )
        assertThat(executableSchema.queryType.name).isEqualTo("Query")
        assertThat(executableSchema.mutationType.name).isEqualTo("M")

        val result =
            CodeGen(
                config().apply {
                    schemas = setOf(schema)
                    this.language = language
                    generateKotlinNullableClasses = closureProjections
                    generateKotlinClosureProjections = closureProjections
                },
            ).generate()

        if (language == Language.JAVA) {
            assertSoftly {
                it.assertThat(result.javaQueryTypes.map { type -> type.typeSpec().name() }).contains("AGraphQLQuery")
                it.assertThat(result.javaConstants.single().toString()).contains("QUERY_TYPE = \"Query\"")
                it.assertThat(result.javaDataTypes.map { type -> type.typeSpec().name() }).doesNotContain("Query")
                it.assertThat(result.javaDataFetchers.map { type -> type.typeSpec().name() }).containsExactly("ADatafetcher")
                it.assertThat(result.docFiles).hasSize(1)
            }
            assertCompilesJava(result)
        } else {
            assertSoftly {
                it.assertThat(result.kotlinConstants.single().toString()).contains("QUERY_TYPE: String = \"Query\"")
                if (closureProjections) {
                    it.assertThat(result.kotlinClientTypes.single { type -> type.name == "DgsClient" }.toString()).contains("buildQuery(")
                } else {
                    it.assertThat(result.javaQueryTypes.map { type -> type.typeSpec().name() }).contains("AGraphQLQuery")
                    it.assertThat(result.kotlinDataTypes.map { type -> type.name }).doesNotContain("Query")
                }
            }
            assertCompilesKotlin(result)
        }
    }

    @ParameterizedTest
    @CsvSource("JAVA,false", "KOTLIN,false", "KOTLIN,true")
    fun `renamed roots retain their data classes`(
        language: Language,
        closureProjections: Boolean,
    ) {
        val result =
            CodeGen(
                config().apply {
                    this.language = language
                    generateKotlinNullableClasses = closureProjections
                    generateKotlinClosureProjections = closureProjections
                },
            ).generate()
        val names =
            if (language ==
                Language.JAVA
            ) {
                result.javaDataTypes.map { it.typeSpec().name() }
            } else {
                result.kotlinDataTypes.map { it.name }
            }
        assertThat(names).contains("RootQuery", "RootMutation")
        if (language == Language.JAVA) assertCompilesJava(result) else assertCompilesKotlin(result)
    }

    @Test
    fun `shared root preserves query outputs`() {
        val sharedSchema = "schema { query: Query mutation: Query } type Query { a: String }"
        val executableSchema =
            SchemaGenerator().makeExecutableSchema(
                SchemaParser().parse(sharedSchema),
                RuntimeWiring.newRuntimeWiring().build(),
            )
        assertThat(executableSchema.queryType).isSameAs(executableSchema.mutationType)
        val result = CodeGen(config().apply { schemas = setOf(sharedSchema) }).generate()
        assertSoftly {
            it.assertThat(result.javaDataFetchers.map { it.typeSpec().name() }).containsExactly("ADatafetcher")
            it
                .assertThat(
                    result.javaConstants
                        .single()
                        .typeSpec()
                        .fieldSpecs()
                        .map { field -> field.name() },
                ).contains("QUERY_TYPE")
            it.assertThat(result.docFiles).hasSize(1)
        }
    }

    @ParameterizedTest
    @CsvSource("JAVA,false,Query", "KOTLIN,false,Query", "KOTLIN,true,Query", "JAVA,false,Root", "KOTLIN,false,Root", "KOTLIN,true,Root")
    fun `shared root retains every operation constant`(
        language: Language,
        closureProjections: Boolean,
        root: String,
    ) {
        val result =
            CodeGen(
                config().apply {
                    schemas = setOf("schema { subscription: $root mutation: $root query: $root } type $root { a: String }")
                    this.language = language
                    generateKotlinNullableClasses = closureProjections
                    generateKotlinClosureProjections = closureProjections
                },
            ).generate()

        if (language == Language.JAVA) {
            val constants =
                result.javaConstants
                    .single()
                    .typeSpec()
                    .fieldSpecs()
            assertThat(constants.map { it.name() }).containsExactly("QUERY_TYPE", "MUTATION_TYPE", "SUBSCRIPTION_TYPE")
            assertThat(constants.map { it.initializer().toString() }).containsOnly("\"$root\"")
            assertThat(result.javaDataFetchers.single().toString()).contains("parentType = \"$root\"")
            assertThat(result.docFiles).hasSize(1)
            assertCompilesJava(result.javaDataFetchers + result.javaConstants + result.javaInterfaces)
            assertCompilesJava(result)
        } else {
            val type =
                result.kotlinConstants
                    .single()
                    .members
                    .single() as TypeSpec
            assertThat(type.propertySpecs.map { it.name }).containsExactly("QUERY_TYPE", "Mutation_TYPE", "Subscription_TYPE")
            assertThat(type.propertySpecs.map { it.initializer.toString() }).containsOnly("\"$root\"")
            assertCompilesKotlin(result)
        }
    }

    private val schema =
        """
        schema { query: RootQuery mutation: RootMutation subscription: RootSubscription }
        type RootQuery { a: String }
        type RootMutation { b: String }
        type RootSubscription { c: String }
        type Query { ordinary: String }
        """.trimIndent()

    @Test
    fun `data fetchers bind to the declared query root`() {
        val result = CodeGen(config()).generate()

        assertThat(result.javaQueryTypes.map { it.typeSpec().name() }).contains("AGraphQLQuery")
        assertThat(result.javaDataFetchers.map { it.typeSpec().name() }).containsExactly("ADatafetcher")
        val fetcher = result.javaDataFetchers.single().toString()
        assertThat(fetcher).contains("parentType = \"RootQuery\"", "field = \"a\"")
        assertCompilesJava(result.javaDataFetchers + result.javaInterfaces)
    }

    @ParameterizedTest
    @CsvSource(
        "JAVA,false,true",
        "KOTLIN,false,true",
        "KOTLIN,true,true",
        "JAVA,false,false",
        "KOTLIN,false,false",
        "KOTLIN,true,false",
    )
    fun `root constants contain declared type names`(
        language: Language,
        closureProjections: Boolean,
        renamedRoots: Boolean,
    ) {
        val result =
            CodeGen(
                config().apply {
                    this.language = language
                    generateKotlinNullableClasses = closureProjections
                    generateKotlinClosureProjections = closureProjections
                    if (!renamedRoots) {
                        schemas =
                            setOf(
                                """
                                type Query { a: String }
                                type Mutation { b: String }
                                type Subscription { c: String }
                                """.trimIndent(),
                            )
                    }
                },
            ).generate()

        val prefix = if (renamedRoots) "Root" else ""
        if (language == Language.JAVA) {
            assertThat(result.javaConstants).hasSize(1)
            val constants =
                result.javaConstants
                    .single()
                    .typeSpec()
                    .fieldSpecs()
                    .associate { it.name() to it.initializer().toString() }
            assertThat(constants).containsAllEntriesOf(
                mapOf(
                    "QUERY_TYPE" to "\"${prefix}Query\"",
                    "MUTATION_TYPE" to "\"${prefix}Mutation\"",
                    "SUBSCRIPTION_TYPE" to "\"${prefix}Subscription\"",
                ),
            )
        } else {
            assertThat(result.kotlinConstants).hasSize(1)
            val type =
                result.kotlinConstants
                    .single()
                    .members
                    .single() as TypeSpec
            val constants = type.propertySpecs.associate { it.name to it.initializer.toString() }
            assertThat(constants).containsAllEntriesOf(
                mapOf(
                    "QUERY_TYPE" to "\"${prefix}Query\"",
                    "Mutation_TYPE" to "\"${prefix}Mutation\"",
                    "Subscription_TYPE" to "\"${prefix}Subscription\"",
                ),
            )
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `query docs follow the declared root when enabled`(enabled: Boolean) {
        val result = CodeGen(config().apply { generateDocs = enabled }).generate()

        if (enabled) {
            assertThat(result.docFiles).hasSize(1)
            val markdown = buildString { result.docFiles.single().writeTo(this) }
            assertThat(markdown).contains("# RootQuery.a:").doesNotContain("Query.ordinary")
        } else {
            assertThat(result.docFiles).isEmpty()
        }
    }

    @ParameterizedTest
    @EnumSource(Language::class)
    fun `explicit schema leaves undeclared Mutation as an ordinary type`(language: Language) {
        val explicitSchema =
            """
            schema { query: Query }
            type Query { a: String }
            type Mutation { ordinary: String }
            """.trimIndent()
        val executableSchema =
            SchemaGenerator().makeExecutableSchema(
                SchemaParser().parse(explicitSchema),
                RuntimeWiring.newRuntimeWiring().build(),
            )
        assertThat(executableSchema.mutationType).isNull()

        val result =
            CodeGen(
                config().apply {
                    schemas = setOf(explicitSchema)
                    this.language = language
                },
            ).generate()
        if (language == Language.JAVA) {
            assertThat(result.javaDataTypes.map { it.typeSpec().name() }).contains("Mutation")
            assertThat(
                result.javaConstants
                    .single()
                    .typeSpec()
                    .fieldSpecs()
                    .map { it.name() },
            ).containsExactly("QUERY_TYPE")
        } else {
            assertThat(result.kotlinDataTypes.map { it.name }).contains("Mutation")
            val constants =
                result.kotlinConstants
                    .single()
                    .members
                    .single() as TypeSpec
            assertThat(constants.propertySpecs.map { it.name }).containsExactly("QUERY_TYPE")
        }
        assertThat(result.javaQueryTypes.map { it.typeSpec().name() }).containsExactly("AGraphQLQuery")
    }

    private fun config(): CodeGenConfig =
        CodeGenConfig(
            schemas = setOf(schema),
            packageName = BASE_PACKAGE_NAME,
            language = Language.JAVA,
            generateClientApi = true,
            generateDataTypes = true,
            generateDocs = true,
        )
}
