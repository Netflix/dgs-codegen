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

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.Serializable

class Kotlin2CodeGenTest {
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

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun generateSealedInterfaces(sealedInterfaces: Boolean) {
        val schema = """
            interface Person {
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
                kotlinv2GenerateSealedInterfaces = sealedInterfaces
            )
        ).generate()

        val interfaces = codeGenResult.kotlinInterfaces

        assertThat(interfaces).hasSize(1)
        assertThat(interfaces.first().members).singleElement()
            .satisfies({ member ->
                val typeSpec = member as TypeSpec
                assertThat(typeSpec.modifiers.contains(KModifier.SEALED)).isEqualTo(sealedInterfaces)
            })
    }
}
