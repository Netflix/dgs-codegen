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

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.Serializable

class Kotline2CodeGenTest {
    @Test
    fun generateSerializableDataClass() {
        val codeGenResult = CodeGen(getTestCodeGenConfig()).generate()

        val dataTypes = codeGenResult.kotlinDataTypes

        assertThat(dataTypes).hasSize(1)
        assertThat(dataTypes.first().packageName).isEqualTo(typesPackageName)
        assertThat(dataTypes.first().members).singleElement()
            .satisfies({ member ->
                val typeSpec = member as TypeSpec
                assertThat(typeSpec.superinterfaces).containsKey(Serializable::class.asClassName())
            })
    }

    @Test
    fun `adds @Deprecated annotation from schema directives when setting enabled`() {
        val schema = """
            enum TownJobTypes {
                LAMPLIGHTER @deprecated(reason: "town switched to electric lights")
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                addDeprecatedAnnotation = true,
                generateKotlinNullableClasses = true
            )
        ).generate()
        val type = result.kotlinEnumTypes[0].members[0] as TypeSpec

        assertThat(FileSpec.get("$basePackageName.enums", type).toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.enums
                |
                |import kotlin.Deprecated
                |
                |public enum class TownJobTypes {
                |  @Deprecated(message = "town switched to electric lights")
                |  LAMPLIGHTER,
                |  ;
                |
                |  public companion object
                |}
                |
            """.trimMargin()

        )
        assertCompilesKotlin(result.kotlinEnumTypes)
    }

    @Test
    fun `Add companion object to enum class`() {
        val schema = """
            enum MyEnum {
                A
                B
                C
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate()

        val type = result.kotlinEnumTypes[0].members[0] as TypeSpec

        assertThat(FileSpec.get("$basePackageName.enums", type).toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.enums
                |
                |public enum class MyEnum {
                |  A,
                |  B,
                |  C,
                |  ;
                |
                |  public companion object
                |}
                |
            """.trimMargin()
        )

        assertCompilesKotlin(result.kotlinEnumTypes)
    }

    @TemplateClassNameTest
    fun generateSerializedDataClassWithCustomName(
        schema: String,
        nameTemplate: String?,
        expectedName: String
    ) {
        val dataTypes = CodeGen(getTestCodeGenConfig(nameTemplate = nameTemplate, schema = schema))
            .generate()
            .kotlinSources()

        assertThat(dataTypes.firstOrNull()?.name).isEqualTo(expectedName)
    }

    @Test
    fun generateSerializedDataClassWithCustomName_InterfaceImplementations() {
        val dataTypes = CodeGen(
            getTestCodeGenConfig(
                nameTemplate = SHOW_INTERFACE_WITH_IMPLEMENTATIONS_NAME_TEMPLATE,
                schema = SHOW_INTERFACE_WITH_IMPLEMENTATIONS_SCHEMA
            )
        )
            .generate()
            .kotlinSources()

        assertThat(dataTypes.map { it.name })
            .containsExactlyInAnyOrderElementsOf(listOf("MovieType", "SeriesType", "ShowInterface", "DgsConstants"))
        assertThat(
            dataTypes.find { it.name == "ShowInterface" }
                ?.typeSpecs
                ?.first()
                ?.annotations
                ?.find { it.canonicalName().endsWith("JsonSubTypes") }
                ?.members
                ?.first()
                ?.toString()
        )
            .contains(
                "Type(value = com.netflix.graphql.dgs.codegen.tests.generated.types.MovieType::class, name = \"Movie\")"
            )
            .contains(
                "Type(value = com.netflix.graphql.dgs.codegen.tests.generated.types.SeriesType::class," +
                    " name = \"Series\")"
            )
    }

    companion object {
        private fun getTestCodeGenConfig(
            nameTemplate: String? = null,
            schema: String = PERSON_TYPE_SCHEMA
        ): CodeGenConfig = CodeGenConfig(
            schemas = setOf(schema),
            packageName = basePackageName,
            language = Language.KOTLIN,
            generateKotlinNullableClasses = true,
            generateKotlinClosureProjections = true,
            implementSerializable = true,
            nameTemplate = nameTemplate
        )
    }
}
