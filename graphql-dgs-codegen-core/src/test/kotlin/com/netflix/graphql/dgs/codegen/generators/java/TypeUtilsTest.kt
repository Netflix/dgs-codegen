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

package com.netflix.graphql.dgs.codegen.generators.java

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import graphql.language.ListType
import graphql.language.TypeName
import graphql.parser.Parser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TypeUtilsTest {
    @Test
    fun `indexed names preserve interface and wildcard type resolution`() {
        val document =
            Parser.parse(
                """
                type Product { id: ID! }
                type IProduct { id: ID! }
                interface Node { id: ID! }
                enum Color { RED }
                union SearchResult = Product
                """.trimIndent(),
            )
        val typeUtils = TypeUtils("com.example", CodeGenConfig(generateInterfaces = true), document)

        assertThat(typeUtils.findReturnType(TypeName("Product"), useInterfaceType = true).toString())
            .isEqualTo("com.example.IProduct")
        assertThat(typeUtils.findReturnType(TypeName("Node"), useInterfaceType = true).toString())
            .isEqualTo("com.example.Node")
        assertThat(typeUtils.findReturnType(TypeName("Color"), useInterfaceType = true).toString())
            .isEqualTo("com.example.Color")
        assertThat(typeUtils.findReturnType(TypeName("SearchResult"), useInterfaceType = true).toString())
            .isEqualTo("com.example.SearchResult")
        assertThat(
            typeUtils
                .findReturnType(
                    ListType(TypeName("Product")),
                    useInterfaceType = true,
                    useWildcardType = true,
                ).toString(),
        ).isEqualTo("java.util.List<? extends com.example.IProduct>")
        assertThat(
            typeUtils
                .findReturnType(
                    ListType(TypeName("IProduct")),
                    useInterfaceType = true,
                    useWildcardType = true,
                ).toString(),
        ).isEqualTo("java.util.List<? extends com.example.IIProduct>")
        assertThat(
            typeUtils
                .findReturnType(
                    ListType(TypeName("Node")),
                    useInterfaceType = true,
                    useWildcardType = true,
                ).toString(),
        ).isEqualTo("java.util.List<? extends com.example.Node>")
    }

    @Test
    fun `schema definitions shadow common scalar mappings`() {
        val document =
            Parser.parse(
                """
                type Date { value: String }
                enum PageInfo { CURRENT }
                """.trimIndent(),
            )
        val typeUtils = TypeUtils("com.example", CodeGenConfig(), document)

        assertThat(typeUtils.findReturnType(TypeName("Date")).toString()).isEqualTo("com.example.Date")
        assertThat(typeUtils.findReturnType(TypeName("PageInfo")).toString()).isEqualTo("com.example.PageInfo")
    }
}
