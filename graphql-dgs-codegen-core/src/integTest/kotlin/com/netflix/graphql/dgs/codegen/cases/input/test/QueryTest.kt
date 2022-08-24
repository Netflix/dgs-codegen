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

package com.netflix.graphql.dgs.codegen.cases.input.test

import com.netflix.graphql.dgs.codegen.cases.input.expected.DgsClient
import com.netflix.graphql.dgs.codegen.cases.input.expected.types.MovieFilter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class QueryTest {

    @Test
    fun testQueryWithNoFilter() {
        val query = DgsClient.buildQuery {
            movies()
        }

        Assertions.assertEquals("""query {
            |  __typename
            |  movies
            |}
            |""".trimMargin(), query)
    }

    @Test
    fun testQueryWithEmptyFilter() {
        val query = DgsClient.buildQuery {
            movies(filter = MovieFilter())
        }

        Assertions.assertEquals("""query {
            |  __typename
            |  movies(filter: {})
            |}
            |""".trimMargin(), query)
    }

    @Test
    fun testQueryWithNullFilter() {
        val query = DgsClient.buildQuery {
            movies(filter = MovieFilter(genre = null))
        }

        Assertions.assertEquals("""query {
            |  __typename
            |  movies(filter: {genre : null})
            |}
            |""".trimMargin(), query)
    }

    @Test
    fun testQueryWithFilter() {
        val query = DgsClient.buildQuery {
            movies(filter = MovieFilter(genre = "horror"))
        }

        Assertions.assertEquals("""query {
            |  __typename
            |  movies(filter: {genre : "horror"})
            |}
            |""".trimMargin(), query)
    }

    @Test
    fun testQueryWithNewline() {
        val query = DgsClient.buildQuery {
            movies(filter = MovieFilter(genre = "horror\ncomedy"))
        }

        Assertions.assertEquals("""query {
            |  __typename
            |  movies(filter: {genre : "horror\ncomedy"})
            |}
            |""".trimMargin(), query)
    }
}
