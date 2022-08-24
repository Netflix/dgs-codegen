/*
 *
 *  Copyright 2022 Netflix, Inc.
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

package com.netflix.graphql.dgs.client.codegen

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GraphQLMultiQueryRequestTest {

    @Test
    fun testSerializeInputClassWithProjectionAndMultipleQueries() {
        val query = TestGraphQLQuery().apply {
            input["movie"] = Movie(1234, "testMovie")
        }
        val query2 = TestGraphQLQuery().apply {
            input["actors"] = "actorA"
            input["movies"] = listOf("movie1", "movie2")
        }

        query.queryAlias = "alias1"
        query2.queryAlias = "alias2"

        val multiRequest = GraphQLMultiQueryRequest(
            listOf(
                GraphQLQueryRequest(query, MovieProjection().name().movieId()),
                GraphQLQueryRequest(query2, MovieProjection().name())
            )
        )

        val result = multiRequest.serialize()
        GraphQLQueryRequestTest.assertValidQuery(result)
        Assertions.assertThat(result).isEqualTo(
            """query {
                |  alias1: test(movie: {movieId : 1234, name : "testMovie"}) {
                |    name
                |    movieId
                |  }
                |  alias2: test(actors: "actorA", movies: ["movie1", "movie2"]) {
                |    name
                |  }
                |}
            """.trimMargin()
        )
    }

    @Test
    fun testSerializeInputClassWithProjectionAndMultipleMutations() {
        val query = TestGraphQLMutation().apply {
            input["movie"] = Movie(1234, "testMovie")
        }
        val query2 = TestGraphQLMutation().apply {
            input["actors"] = "actorA"
            input["movies"] = listOf("movie1", "movie2")
        }
        val query3 = TestGraphQLMutation().apply {
            input["actors"] = "actorA"
            input["movies"] = listOf("movie1", "movie2")
        }

        query.queryAlias = "alias1"
        query2.queryAlias = "alias2"
        query3.queryAlias = "alias3"

        val multiRequest = GraphQLMultiQueryRequest(
            listOf(
                GraphQLQueryRequest(query),
                GraphQLQueryRequest(query2),
                GraphQLQueryRequest(query3)
            )
        )

        val result = multiRequest.serialize()
        GraphQLQueryRequestTest.assertValidQuery(result)
        Assertions.assertThat(result).isEqualTo(
            """mutation {
              |  alias1: testMutation(movie: {movieId : 1234, name : "testMovie"})
              |  alias2: testMutation(actors: "actorA", movies: ["movie1", "movie2"])
              |  alias3: testMutation(actors: "actorA", movies: ["movie1", "movie2"])
              |}
            """.trimMargin()
        )
    }

    @Test
    fun testSerializeInputClassWithProjectionAndMultipleMutations_MismatchOperationType() {
        val query = TestGraphQLMutation().apply {
            input["movie"] = Movie(1234, "testMovie")
        }

        val query2 = TestGraphQLQuery().apply {
            input["actors"] = "actorA"
            input["movies"] = listOf("movie1", "movie2")
        }

        val multiRequest = GraphQLMultiQueryRequest(
            listOf(
                GraphQLQueryRequest(query),
                GraphQLQueryRequest(query2)
            )
        )

        assertThrows<AssertionError> {
            multiRequest.serialize()
        }
    }

    @Test
    fun testSerializeInputClassWithProjectionAndSingleQueriesAndAlias() {
        val query = TestGraphQLQuery().apply {
            input["movie"] = Movie(1234, "testMovie")
        }

        query.queryAlias = "alias1"

        val multiRequest = GraphQLMultiQueryRequest(
            listOf(
                GraphQLQueryRequest(query, MovieProjection().name().movieId())
            )
        )

        val result = multiRequest.serialize()
        GraphQLQueryRequestTest.assertValidQuery(result)
        Assertions.assertThat(result).isEqualTo(
            """query {
                |  alias1: test(movie: {movieId : 1234, name : "testMovie"}) {
                |    name
                |    movieId
                |  }
                |}
            """.trimMargin()
        )
    }
}
