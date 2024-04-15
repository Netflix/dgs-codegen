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
        val schema = """
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                generateKotlinNullableClasses = true,
                generateKotlinClosureProjections = true,
                implementSerializable = true
            )
        ).generate()

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
}
