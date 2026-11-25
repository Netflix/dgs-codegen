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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URLClassLoader

class KotlinJsonPropertyTest {
    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `explicit field targets compile without warnings and preserve Jackson binding`(nullableClasses: Boolean) {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas =
                        setOf(
                            """
                            input ExampleInput {
                                _: String!
                                isActive: Boolean!
                                optional: String
                                count: Int = 7
                            }
                            type Example {
                                _: String!
                                isActive: Boolean!
                                optional: String
                            }
                            """.trimIndent(),
                        ),
                    packageName = "com.netflix.test.jsontargets",
                    language = Language.KOTLIN,
                    generateKotlinNullableClasses = nullableClasses,
                ),
            ).generate()

        val inputSource = result.kotlinSources().single { it.name == "ExampleInput" }.toString()
        assertThat(inputSource).contains(
            "@param:JsonProperty(\"_\")",
            "@field:JsonProperty(\"_\")",
            "@param:JsonProperty(\"isActive\")",
            "@field:JsonProperty(\"isActive\")",
        )
        val buildDir = assertCompilesKotlin(result, warningsAsErrors = true)
        URLClassLoader(arrayOf(buildDir.toUri().toURL()), javaClass.classLoader).use { loader ->
            val mapper = jacksonObjectMapper()
            val inputClass = loader.loadClass("com.netflix.test.jsontargets.types.ExampleInput")
            assertThat(inputClass.declaredFields.mapNotNull { it.getAnnotation(JsonProperty::class.java)?.value })
                .containsExactly("_", "isActive", "optional", "count")
            // Plain Jackson (no Kotlin module) must still bind required constructor arguments.
            val plainMapper = ObjectMapper()
            val plainInput = plainMapper.readValue("""{"_":"value","isActive":true,"optional":null,"count":7}""", inputClass)
            assertThat(plainMapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(plainInput).path("_").asText())
                .isEqualTo("value")
            if (!nullableClasses) {
                val outputClass = loader.loadClass("com.netflix.test.jsontargets.types.Example")
                val plainOutput = plainMapper.readValue("""{"_":"result","isActive":false,"optional":"present"}""", outputClass)
                assertThat(outputClass.getMethod("getUnderscoreField_").invoke(plainOutput)).isEqualTo("result")
            }
            val input = mapper.readValue("""{"_":"value","isActive":true,"optional":null}""", inputClass)
            val json = mapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(input)
            assertThat(json.path("_").asText()).isEqualTo("value")
            assertThat(json.path("isActive").asBoolean()).isTrue()
            assertThat(json.path("optional").isNull).isTrue()
            assertThat(json.path("count").asInt()).isEqualTo(7)
            assertThat(json.has("underscoreField_")).isFalse()

            val outputClass = loader.loadClass("com.netflix.test.jsontargets.types.Example")
            val output = mapper.readValue("""{"_":"result","isActive":false,"optional":"present"}""", outputClass)
            assertThat(outputClass.getMethod("getUnderscoreField_").invoke(output)).isEqualTo("result")
            assertThat(outputClass.getMethod(if (nullableClasses) "getIsActive" else "isActive").invoke(output)).isEqualTo(false)
            assertThat(outputClass.getMethod("getOptional").invoke(output)).isEqualTo("present")
            if (nullableClasses) {
                val builder = outputClass.declaredClasses.single { it.simpleName == "Builder" }
                assertThat(builder.getMethod("withUnderscoreField_", String::class.java).getAnnotation(JsonProperty::class.java).value)
                    .isEqualTo("_")
            } else {
                assertThat(outputClass.getDeclaredField("underscoreField_").getAnnotation(JsonProperty::class.java).value)
                    .isEqualTo("_")
                val outputJson = mapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(output)
                assertThat(outputJson.path("_").asText()).isEqualTo("result")
                assertThat(outputJson.path("isActive").asBoolean()).isFalse()
                assertThat(outputJson.path("optional").asText()).isEqualTo("present")
            }
        }
    }
}
