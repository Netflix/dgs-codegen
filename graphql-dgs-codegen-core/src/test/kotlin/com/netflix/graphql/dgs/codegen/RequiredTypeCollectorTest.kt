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

import graphql.parser.Parser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RequiredTypeCollectorTest {
    private val document = Parser.parse("""
            type Query {
                search(filter: Filter): [Show]
                shows(type: ShowType!): [Show]
                searchByExample(example: ExampleShow): [Show]
                searchByExamples(examples: [ExampleShow]): [Show]
            }
            
            input Filter {
                title: String
                type: ShowType
            }
            
            enum ShowType {
                SERIES,
                MOVIE
            }
            
            input ExampleShow {
                title: String
                actor: ExampleActor
                type: ShowType
            }               
               
            input ExampleActor {
                name: String
            }
        """.trimIndent())

    @Test
    fun `Related input types and enums should be included`() {

        val types = RequiredTypeCollector(document, setOf("search")).requiredTypes

        assertThat(types).contains("Filter", "ShowType")
    }

    @Test
    fun `Related nested input types and enums should be included`() {

        val types = RequiredTypeCollector(document, setOf("searchByExample")).requiredTypes

        assertThat(types).contains("ExampleShow", "ExampleActor", "ShowType")
    }

    @Test
    fun `Unrelated input types should be omitted`() {

        val types = RequiredTypeCollector(document, setOf("searchByExample")).requiredTypes

        assertThat(types).doesNotContain("Filter")
    }

    @Test
    fun `All related input types should be included for multiple queries`() {

        val types = RequiredTypeCollector(document, setOf("searchByExample", "search")).requiredTypes

        assertThat(types).contains("ExampleShow", "Filter", "ShowType", "ExampleActor")
    }

    @Test
    fun `Lists of required input types should be included`() {

        val types = RequiredTypeCollector(document, setOf("searchByExamples")).requiredTypes
        assertThat(types).contains("ExampleShow", "ExampleActor", "ShowType")
    }
}