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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class FloatDefaultValueTest {
    @ParameterizedTest
    @ValueSource(strings = ["JAVA", "KOTLIN", "KOTLIN2"])
    fun `integer Float defaults compile in every generator`(generator: String) {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            """
                            input ExampleInput {
                                ratio: Float = 1
                                minRatio: Float! = 0
                                negative: Float = -2
                                ratios: [Float] = [1, 2.5, null, -3]
                                requiredRatios: [Float!]! = [0, 2]
                                nested: NestedInput = {ratio: 4, ratios: [5, 6.5]}
                                count: Int = 1
                                counts: [Int] = [1, 2]
                            }
                            input NestedInput {
                                ratio: Float
                                ratios: [Float]
                            }
                            """.trimIndent(),
                        ),
                    packageName = BASE_PACKAGE_NAME,
                    language = if (generator == "JAVA") Language.JAVA else Language.KOTLIN,
                    generateKotlinNullableClasses = generator == "KOTLIN2",
                ),
            ).generate()

        if (generator == "JAVA") {
            val type = result.javaDataTypes.single { it.typeSpec().name() == "ExampleInput" }.typeSpec()
            val fields = type.fieldSpecs().associate { it.name() to it.initializer().toString() }
            assertThat(fields).containsAllEntriesOf(
                mapOf(
                    "ratio" to "1.0",
                    "minRatio" to "0.0",
                    "negative" to "-2.0",
                    "ratios" to "java.util.Arrays.asList(1.0, 2.5, null, -3.0)",
                    "requiredRatios" to "java.util.Arrays.asList(0.0, 2.0)",
                    "count" to "1",
                    "counts" to "java.util.Arrays.asList(1, 2)",
                ),
            )
            val builder = type.typeSpecs().single { it.name() == "Builder" }
            fields.forEach { (name, initializer) ->
                val builderInitializer =
                    builder
                        .fieldSpecs()
                        .single { it.name() == name }
                        .initializer()
                        .toString()
                assertThat(builderInitializer).isEqualTo(initializer)
            }
            assertCompilesJava(result)
        } else {
            val source = result.kotlinSources().joinToString("\n")
            assertThat(source).contains("listOf(1.0, 2.5, null, -3.0)", "listOf(0.0, 2.0)", "listOf(1, 2)")
            assertCompilesKotlin(result)
        }
    }
}
