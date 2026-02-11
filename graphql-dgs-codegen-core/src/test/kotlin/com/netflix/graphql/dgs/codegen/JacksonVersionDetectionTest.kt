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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JacksonVersionDetectionTest {
    private val schema =
        """
        type Query {
            movies: [Movie]
        }

        type Movie {
            title: String
            director: String
        }
        """.trimIndent()

    @Test
    fun `generates only Jackson 2 JsonDeserialize and JsonPOJOBuilder annotations when Jackson 2 is configured`() {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = "com.test",
                    language = Language.KOTLIN,
                    generateKotlinNullableClasses = true,
                    jacksonVersions = setOf(JacksonVersion.JACKSON_2),
                ),
            ).generate()

        val movieType = result.kotlinDataTypes.first { it.name == "Movie" }
        val fileContent = movieType.toString()

        assertThat(fileContent).contains("com.fasterxml.jackson.databind.`annotation`.JsonDeserialize")
        assertThat(fileContent).doesNotContain("tools.jackson.databind.`annotation`.JsonDeserialize")

        assertThat(fileContent).contains("com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder")
        assertThat(fileContent).doesNotContain("tools.jackson.databind.`annotation`.JsonPOJOBuilder")
    }

    @Test
    fun `generates only Jackson 3 JsonDeserialize and JsonPOJOBuilder annotations when Jackson 3 is configured`() {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = "com.test",
                    language = Language.KOTLIN,
                    generateKotlinNullableClasses = true,
                    jacksonVersions = setOf(JacksonVersion.JACKSON_3),
                ),
            ).generate()

        val movieType = result.kotlinDataTypes.first { it.name == "Movie" }
        val fileContent = movieType.toString()

        assertThat(fileContent).contains("tools.jackson.databind.`annotation`.JsonDeserialize")
        assertThat(fileContent).doesNotContain("com.fasterxml.jackson.databind.`annotation`.JsonDeserialize")

        assertThat(fileContent).contains("tools.jackson.databind.`annotation`.JsonPOJOBuilder")
        assertThat(fileContent).doesNotContain("com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder")
    }

    @Test
    fun `generates both Jackson 2 and 3 JsonDeserialize and JsonPOJOBuilder annotations when both are configured`() {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = "com.test",
                    language = Language.KOTLIN,
                    generateKotlinNullableClasses = true,
                    jacksonVersions = setOf(JacksonVersion.JACKSON_2, JacksonVersion.JACKSON_3),
                ),
            ).generate()

        val movieType = result.kotlinDataTypes.first { it.name == "Movie" }
        val fileContent = movieType.toString()

        assertThat(fileContent).contains("com.fasterxml.jackson.databind.`annotation`.JsonDeserialize")
        assertThat(fileContent).contains("tools.jackson.databind.`annotation`.JsonDeserialize")

        assertThat(fileContent).contains("tools.jackson.databind.`annotation`.JsonPOJOBuilder")
        assertThat(fileContent).contains("com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder")

        assertThat(fileContent).contains("@ToolsJacksonDatabindAnnotationJsonPOJOBuilder")
        assertThat(fileContent).contains("@FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder")

        assertThat(fileContent).contains("@ToolsJacksonDatabindAnnotationJsonDeserialize")
        assertThat(fileContent).contains("@FasterxmlJacksonDatabindAnnotationJsonDeserialize")
    }

    @Test
    fun `defaults to Jackson 2 when no configuration is provided`() {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = "com.test",
                    language = Language.KOTLIN,
                    generateKotlinNullableClasses = true,
                ),
            ).generate()

        val movieType = result.kotlinDataTypes.first { it.name == "Movie" }
        val fileContent = movieType.toString()

        assertThat(fileContent).contains("com.fasterxml.jackson.databind.`annotation`.JsonDeserialize")
        assertThat(fileContent).doesNotContain("tools.jackson.databind.`annotation`.JsonDeserialize")

        assertThat(fileContent).contains("com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder")
        assertThat(fileContent).doesNotContain("tools.jackson.databind.`annotation`.JsonPOJOBuilder")
    }

    @Test
    fun `empty configuration defaults to Jackson 2`() {
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = "com.test",
                    language = Language.KOTLIN,
                    generateKotlinNullableClasses = true,
                    jacksonVersions = emptySet(),
                ),
            ).generate()

        val movieType = result.kotlinDataTypes.first { it.name == "Movie" }
        val fileContent = movieType.toString()

        // Should default to Jackson 2 (backwards compatibility)
        assertThat(fileContent).contains("com.fasterxml.jackson.databind.`annotation`.JsonDeserialize")
        assertThat(fileContent).doesNotContain("tools.jackson.databind.`annotation`.JsonDeserialize")

        assertThat(fileContent).contains("com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder")
        assertThat(fileContent).doesNotContain("tools.jackson.databind.`annotation`.JsonPOJOBuilder")
    }
}
