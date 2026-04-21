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

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JavaDeserializationTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `interface deserializes to concrete subtype based on __typename`() {
        val schema =
            """
            type Query {
                search: Show
            }

            interface Show {
                title: String
            }

            type Movie implements Show {
                title: String
                duration: Int
            }

            type Series implements Show {
                title: String
                episodes: Int
            }
            """.trimIndent()

        val codeGenResult =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = BASE_PACKAGE_NAME,
                ),
            ).generate()

        val testClassLoader = assertCompilesJava(codeGenResult).toClassLoader()
        val showInterface = testClassLoader.loadClass("$TYPES_PACKAGE_NAME.Show")
        val movieClass = testClassLoader.loadClass("$TYPES_PACKAGE_NAME.Movie")

        val json = """{"__typename":"Movie","title":"The Matrix","duration":136}"""
        val instance = objectMapper.readValue(json, showInterface)

        assertThat(instance).isInstanceOf(movieClass)
        assertThat(movieClass.getMethod("getTitle").invoke(instance)).isEqualTo("The Matrix")
        assertThat(movieClass.getMethod("getDuration").invoke(instance)).isEqualTo(136)
    }

    @Test
    fun `union deserializes to concrete subtype based on __typename`() {
        val schema =
            """
            type Query {
                search: Video
            }

            union Video = Movie | Series

            type Movie {
                title: String
                duration: Int
            }

            type Series {
                title: String
                episodes: Int
            }
            """.trimIndent()

        val codeGenResult =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = BASE_PACKAGE_NAME,
                ),
            ).generate()

        val testClassLoader = assertCompilesJava(codeGenResult).toClassLoader()
        val videoInterface = testClassLoader.loadClass("$TYPES_PACKAGE_NAME.Video")
        val seriesClass = testClassLoader.loadClass("$TYPES_PACKAGE_NAME.Series")

        val json = """{"__typename":"Series","title":"Arrested Development","episodes":68}"""
        val instance = objectMapper.readValue(json, videoInterface)

        assertThat(instance).isInstanceOf(seriesClass)
        assertThat(seriesClass.getMethod("getTitle").invoke(instance)).isEqualTo("Arrested Development")
        assertThat(seriesClass.getMethod("getEpisodes").invoke(instance)).isEqualTo(68)
    }
}
