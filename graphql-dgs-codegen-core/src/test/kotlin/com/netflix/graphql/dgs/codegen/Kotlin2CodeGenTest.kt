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

package com.netflix.graphql.dgs.codegen

import com.netflix.graphql.dgs.codegen.generators.kotlin.sanitizeKotlinIdentifier
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import graphql.language.Field
import graphql.language.NullValue
import graphql.language.OperationDefinition
import graphql.language.StringValue
import graphql.parser.Parser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.Serializable
import java.net.URLClassLoader

class Kotlin2CodeGenTest {
    @Test
    fun generateSerializableDataClass() {
        val schema =
            """
            type Person {
                firstname: String
                lastname: String
            }
            """.trimIndent()

        val codeGenResult =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = BASE_PACKAGE_NAME,
                    language = Language.KOTLIN,
                    generateKotlinNullableClasses = true,
                    generateKotlinClosureProjections = true,
                    implementSerializable = true,
                ),
            ).generate()

        val dataTypes = codeGenResult.kotlinDataTypes

        assertThat(dataTypes).hasSize(1)
        assertThat(dataTypes.first().packageName).isEqualTo(TYPES_PACKAGE_NAME)
        assertThat(dataTypes.first().members)
            .singleElement()
            .satisfies({ member ->
                val typeSpec = member as TypeSpec
                assertThat(typeSpec.superinterfaces).containsKey(Serializable::class.asClassName())
            })
    }

    @Test
    fun `adds @Deprecated annotation from schema directives when setting enabled`() {
        val schema =
            """
            enum TownJobTypes {
                LAMPLIGHTER @deprecated(reason: "town switched to electric lights")
            }
            """.trimIndent()

        val result =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = BASE_PACKAGE_NAME,
                    language = Language.KOTLIN,
                    addDeprecatedAnnotation = true,
                    generateKotlinNullableClasses = true,
                ),
            ).generate()
        val type = result.kotlinEnumTypes[0].members[0] as TypeSpec

        assertThat(FileSpec.get("$BASE_PACKAGE_NAME.enums", type).toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.enums
                |
                |import com.netflix.graphql.dgs.codegen.tests.generated.Generated
                |import kotlin.Deprecated
                |
                |@Generated
                |public enum class TownJobTypes {
                |  @Generated
                |  @Deprecated(message = "town switched to electric lights")
                |  LAMPLIGHTER,
                |  ;
                |
                |  @Generated
                |  public companion object
                |}
                |
            """.trimMargin(),
        )
        assertCompilesKotlin(result)
    }

    @Test
    fun `Add companion object to enum class`() {
        val schema =
            """
            enum MyEnum {
                A
                B
                C
            }
            """.trimIndent()

        val result =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = BASE_PACKAGE_NAME,
                    language = Language.KOTLIN,
                ),
            ).generate()

        val type = result.kotlinEnumTypes[0].members[0] as TypeSpec

        assertThat(FileSpec.get("$BASE_PACKAGE_NAME.enums", type).toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.enums
                |
                |import com.netflix.graphql.dgs.codegen.tests.generated.Generated
                |
                |@Generated
                |public enum class MyEnum {
                |  A,
                |  B,
                |  C,
                |  ;
                |
                |  @Generated
                |  public companion object
                |}
                |
            """.trimMargin(),
        )

        assertCompilesKotlin(result)
    }

    @ParameterizedTest
    @MethodSource("projectionParameterNames")
    fun `projection parameters do not shadow schema arguments`(
        operationType: String,
        argumentNames: List<String>,
        aliasParameterName: String,
        projectionParameterName: String,
    ) {
        val arguments = argumentNames.joinToString(", ") { "$it: String" }
        val field = if (arguments.isEmpty()) "nodeSetCreate" else "nodeSetCreate($arguments)"
        val schema =
            """
            type $operationType {
                $field: NodeResult
            }

            type NodeResult {
                id: String
            }
            """.trimIndent()

        val result = generateClient(schema)

        val operationProjection =
            result.kotlinClientTypes
                .single { it.name == "${operationType}Projection" }
                .members
                .single() as TypeSpec
        val method = operationProjection.funSpecs.single { it.name == "nodeSetCreate" }
        assertThat(method.parameters.map { it.name }).containsExactlyElementsOf(
            argumentNames.map(::sanitizeKotlinIdentifier) + listOf(aliasParameterName, projectionParameterName),
        )

        val suppliedArgs = argumentNames.map { "`${sanitizeKotlinIdentifier(it)}` = \"$it value\"" }
        val nullArgs = argumentNames.map { "`${sanitizeKotlinIdentifier(it)}` = null" }
        val aliasArg = "$aliasParameterName = \"myAlias\""
        val projectionArg = "$projectionParameterName = { id }"
        val suppliedCall = (suppliedArgs + aliasArg + projectionArg).joinToString(", ")
        val nullCall = (nullArgs + "$aliasParameterName = \"nullAlias\"").joinToString(", ")
        val testFile =
            """
            package $BASE_PACKAGE_NAME

            fun runQuery(mode: Int): String {
                return DgsClient.build$operationType {
                    when (mode) {
                        0 -> nodeSetCreate($suppliedCall)
                        1 -> nodeSetCreate { id }
                        else -> nodeSetCreate($nullCall) { id }
                    }
                }
            }
            """.trimIndent()

        val buildDir = assertCompilesKotlin(result, mapOf("TestQuery.kt" to testFile))
        URLClassLoader(arrayOf(buildDir.toUri().toURL()), javaClass.classLoader).use { loader ->
            val runQuery = loader.loadClass("$BASE_PACKAGE_NAME.TestQueryKt").getMethod("runQuery", Int::class.javaPrimitiveType)
            val fields =
                (0..2).map { mode ->
                    val query = runQuery.invoke(null, mode) as String
                    Parser()
                        .parseDocument(query)
                        .getDefinitionsOfType(OperationDefinition::class.java)
                        .single()
                        .selectionSet.selections
                        .filterIsInstance<Field>()
                        .single { it.name == "nodeSetCreate" }
                }
            assertThat(fields[0].alias).isEqualTo("myAlias")
            assertThat(fields[0].arguments.associate { it.name to (it.value as StringValue).value })
                .containsExactlyInAnyOrderEntriesOf(argumentNames.associateWith { "$it value" })
            assertThat(
                fields[0]
                    .selectionSet.selections
                    .filterIsInstance<Field>()
                    .map { it.name },
            ).containsExactly("__typename", "id")
            assertThat(fields[1].alias).isNull()
            assertThat(fields[1].arguments).isEmpty()
            assertThat(fields[2].alias).isEqualTo("nullAlias")
            assertThat(fields[2].arguments.map { it.name }).containsExactlyElementsOf(argumentNames)
            assertThat(fields[2].arguments).allMatch { it.value is NullValue }
        }
    }

    @Test
    fun `schema arguments do not shadow serializer or helper references`() {
        val schema =
            """
            type Query {
                node(inputValueSerializer: String): Node
            }
            type Node {
                leaf(inputValueSerializer: String, field: String, default: String, omitted: String): String
            }
            """.trimIndent()
        val result = generateClient(schema)

        val testFile =
            """
            package $BASE_PACKAGE_NAME

            import com.netflix.graphql.dgs.client.codegen.InputValueSerializer
            import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface

            fun runQuery(): String {
                val delegate = InputValueSerializer()
                val serializer = object : InputValueSerializerInterface by delegate {
                    override fun toValue(input: Any?) =
                        delegate.toValue(if (input is String) "serialized:" + input else input)
                }
                return DgsClient.buildQuery(inputValueSerializer = serializer) {
                    node(inputValueSerializer = "root") {
                        leaf(inputValueSerializer = "child", field = "fieldValue", default = "defaultValue")
                    }
                }
            }
            """.trimIndent()

        val buildDir = assertCompilesKotlin(result, mapOf("TestQuery.kt" to testFile))
        URLClassLoader(arrayOf(buildDir.toUri().toURL()), javaClass.classLoader).use { loader ->
            val query = loader.loadClass("$BASE_PACKAGE_NAME.TestQueryKt").getMethod("runQuery").invoke(null) as String
            assertThat(query).contains("node(inputValueSerializer: \"serialized:root\")")
            assertThat(query)
                .contains(
                    "leaf(inputValueSerializer: \"serialized:child\",",
                    "field: \"serialized:fieldValue\"",
                    "default: \"serialized:defaultValue\"",
                ).doesNotContain("omitted:")
        }
    }

    private fun generateClient(schema: String): CodeGenResult =
        CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = BASE_PACKAGE_NAME,
                language = Language.KOTLIN,
                generateClientApi = true,
                generateKotlinNullableClasses = true,
                generateKotlinClosureProjections = true,
            ),
        ).generate()

    companion object {
        @JvmStatic
        fun projectionParameterNames(): List<Arguments> =
            listOf(
                Arguments.of("Query", emptyList<String>(), "_alias", "_projection"),
                Arguments.of("Mutation", listOf("_alias"), "_alias_", "_projection"),
                Arguments.of("Subscription", listOf("_projection"), "_alias", "_projection_"),
                Arguments.of("Mutation", listOf("_alias", "_projection", "summary"), "_alias_", "_projection_"),
                Arguments.of(
                    "Query",
                    listOf("_alias", "_alias_", "_alias__", "_projection", "_projection_", "_projection__"),
                    "_alias___",
                    "_projection___",
                ),
                Arguments.of("Query", listOf("_alias_", "_projection_"), "_alias", "_projection"),
                Arguments.of("Query", listOf("_", "when", "field", "default"), "_alias", "_projection"),
            )
    }
}
