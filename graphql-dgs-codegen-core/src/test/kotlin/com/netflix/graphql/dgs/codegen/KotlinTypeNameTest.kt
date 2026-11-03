/*
 * Copyright 2026 Netflix, Inc.
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

import com.netflix.graphql.dgs.codegen.generators.kotlin.parseMappedKotlinType
import com.netflix.graphql.dgs.codegen.generators.kotlin.toKtTypeName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.STRING
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class KotlinTypeNameTest {
    @ParameterizedTest(name = "{0}, nullable={1}, closures={2}, client={3}")
    @MethodSource("optionCombinations")
    fun `preserve object names and reference nullability across generator options`(
        name: String,
        nullableClasses: Boolean,
        closureProjections: Boolean,
        client: Boolean,
    ) {
        val result =
            generate(
                schema =
                    """
                    type Query { holder: Holder }
                    type Holder {
                        optional: $name
                        required: $name!
                        optionalList: [$name]
                        requiredList: [$name!]!
                        nested: [[$name!]!]!
                    }
                    type $name { name: String }
                    """.trimIndent(),
                nullableClasses = nullableClasses,
                closureProjections = closureProjections,
                client = client,
            )

        assertThat(result.kotlinDataTypes.map { it.name }).contains(name)
        val holder =
            result.kotlinDataTypes
                .single { it.name == "Holder" }
                .typeSpecs
                .single()
        val properties = holder.propertySpecs.associate { it.name to it.type }
        val type = ClassName(TYPES_PACKAGE_NAME, name)
        assertThat(properties).containsAllEntriesOf(
            mapOf(
                "optional" to type.copy(nullable = true),
                "required" to type,
                "optionalList" to LIST.parameterizedBy(type.copy(nullable = true)).copy(nullable = true),
                "requiredList" to LIST.parameterizedBy(type),
                "nested" to LIST.parameterizedBy(LIST.parameterizedBy(type)),
            ),
        )
        assertThat(result.kotlinClientTypes.isNotEmpty()).isEqualTo(client && closureProjections)
        assertThat(result.javaQueryTypes.isNotEmpty()).isEqualTo(client && !closureProjections)
    }

    @ParameterizedTest(name = "{0}: {1}, nullable={3}")
    @MethodSource("schemaCases")
    fun `both Kotlin generators compile every schema reference kind`(
        name: String,
        kind: String,
        schema: String,
        nullableClasses: Boolean,
    ) {
        val result = generate(schema, nullableClasses = nullableClasses)

        assertThat(result.kotlinSources().map { it.name }).`as`(kind).contains(name)
        assertCompilesKotlin(result)
    }

    @ParameterizedTest(name = "{0}, nullable={1}")
    @MethodSource("projectionCombinations")
    fun `closure projections preserve input and enum argument names`(
        name: String,
        nullableClasses: Boolean,
    ) {
        val result =
            generate(
                schema =
                    """
                    type Query {
                        item(filter: ${name}Input!, values: [${name}Input!], state: ${name}State): Item
                    }
                    type Item { id: ID! }
                    input ${name}Input { value: String }
                    enum ${name}State { ACTIVE }
                    """.trimIndent(),
                nullableClasses = nullableClasses,
                closureProjections = true,
                client = true,
            )

        val query =
            result.kotlinClientTypes
                .single { it.name == "QueryProjection" }
                .typeSpecs
                .single()
        val item = query.funSpecs.single { it.name == "item" }
        assertThat(item.parameters.single { it.name == "filter" }.type)
            .isEqualTo(ClassName(TYPES_PACKAGE_NAME, "${name}Input"))
        assertThat(item.parameters.single { it.name == "state" }.type)
            .isEqualTo(ClassName(TYPES_PACKAGE_NAME, "${name}State").copy(nullable = true))
        assertCompilesKotlin(result)
    }

    @Test
    fun `star is accepted only as a generic argument`() {
        assertThat(parseMappedKotlinType("List<*>")).isEqualTo(LIST.parameterizedBy(STAR))
        assertThat(parseMappedKotlinType("List<String?>")).isEqualTo(LIST.parameterizedBy(STRING.copy(nullable = true)))
        assertThat("*".toKtTypeName(isGenericParam = true)).isEqualTo(STAR)
        assertThat("String?".toKtTypeName().isNullable).isTrue()
        assertThat("String".toKtTypeName()).isEqualTo(STRING)
        assertThatThrownBy { "*".toKtTypeName() }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { parseMappedKotlinType("*") }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @ParameterizedTest
    @MethodSource("typeNames")
    fun `Java generation accepts the same names`(name: String) {
        val result =
            generate(
                schema = "type Holder { value: $name } type $name { name: String }",
                language = Language.JAVA,
            )

        assertThat(result.javaDataTypes.map { it.typeSpec().name() }).contains(name)
        assertCompilesJava(result)
    }

    private fun generate(
        schema: String,
        language: Language = Language.KOTLIN,
        nullableClasses: Boolean = false,
        closureProjections: Boolean = false,
        client: Boolean = false,
    ): CodeGenResult =
        CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = BASE_PACKAGE_NAME,
                language = language,
                generateKotlinNullableClasses = nullableClasses,
                generateKotlinClosureProjections = closureProjections,
                generateClientApi = client,
            ),
        ).generate()

    companion object {
        @JvmStatic
        fun typeNames(): List<String> = listOf("Person", "person", "personWithCamelCase", "_Person", "_person")

        @JvmStatic
        fun optionCombinations(): List<Arguments> =
            typeNames().flatMap { name ->
                listOf(false, true).flatMap { nullable ->
                    listOf(false, true).flatMap { closures ->
                        listOf(false, true).map { client -> Arguments.of(name, nullable, closures, client) }
                    }
                }
            }

        @JvmStatic
        fun projectionCombinations(): List<Arguments> =
            typeNames().flatMap { name -> listOf(false, true).map { nullable -> Arguments.of(name, nullable) } }

        @JvmStatic
        fun schemaCases(): List<Arguments> =
            typeNames().flatMap { name ->
                listOf(
                    "standalone builder" to "type $name { name: String }",
                    "recursive object" to "type $name { next: $name children: [$name!]! }",
                    "input" to "input $name { next: $name } input Envelope { value: $name }",
                    "enum" to "enum $name { ACTIVE } type Holder { value: $name }",
                    "implemented interface" to "interface $name { id: ID } type Item implements $name { id: ID }",
                    "inherited interface" to
                        """
                        interface $name { id: ID }
                        interface Child implements $name { id: ID }
                        type Item implements $name & Child { id: ID }
                        """.trimIndent(),
                    "union" to "union $name = Item type Item { id: ID }",
                ).flatMap { (kind, schema) ->
                    listOf(false, true).map { nullable -> Arguments.of(name, kind, schema, nullable) }
                }
            }
    }
}
