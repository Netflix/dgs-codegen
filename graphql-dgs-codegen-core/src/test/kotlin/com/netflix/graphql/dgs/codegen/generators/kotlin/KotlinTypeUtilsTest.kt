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

package com.netflix.graphql.dgs.codegen.generators.kotlin

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import graphql.language.Document
import graphql.language.TypeName
import graphql.parser.Parser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KotlinTypeUtilsTest {
    @Test
    fun `schema object and enum definitions shadow common scalar mappings`() {
        assertThat(findType("Date", "type Date { value: String }"))
            .isEqualTo("com.example.types.Date?")
        assertThat(findType("PageInfo", "enum PageInfo { CURRENT }"))
            .isEqualTo("com.example.types.PageInfo?")
    }

    @Test
    fun `common scalar mappings retain precedence for scalar declarations`() {
        assertThat(
            KotlinTypeUtils("com.example.types", CodeGenConfig(packageName = "com.example"), Document(emptyList()))
                .findReturnType(TypeName("Date"))
                .toString(),
        ).isEqualTo("java.time.LocalDate?")
        assertThat(findType("Date", "scalar Date"))
            .isEqualTo("java.time.LocalDate?")
    }

    @Test
    fun `type extensions retain the existing common scalar shadowing behavior`() {
        assertThat(findType("Date", "extend type Date { value: String }"))
            .isEqualTo("com.example.types.Date?")
        assertThat(findType("PageInfo", "extend enum PageInfo { CURRENT }"))
            .isEqualTo("com.example.types.PageInfo?")
    }

    @Test
    fun `explicit type mappings take precedence over indexed schema definitions`() {
        val utils =
            KotlinTypeUtils(
                "com.example.types",
                CodeGenConfig(packageName = "com.example", typeMapping = mapOf("Date" to "com.example.CustomDate")),
                Parser.parse("type Date { value: String }"),
            )

        assertThat(utils.findReturnType(TypeName("Date")).toString())
            .isEqualTo("com.example.CustomDate?")
    }

    @Test
    fun `schema type mappings take precedence over common scalar mappings`() {
        val utils =
            KotlinTypeUtils(
                "com.example.types",
                CodeGenConfig(packageName = "com.example"),
                Parser.parse("scalar Date @javaType(name: \"com.example.SchemaDate\")"),
            )

        assertThat(utils.findReturnType(TypeName("Date")).toString())
            .isEqualTo("com.example.SchemaDate?")
    }

    private fun findType(
        name: String,
        schema: String,
    ): String =
        KotlinTypeUtils(
            "com.example.types",
            CodeGenConfig(packageName = "com.example"),
            Parser.parse(schema),
        ).findReturnType(TypeName(name)).toString()
}
