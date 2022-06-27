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

package com.netflix.graphql.dgs.client.codegen

import com.netflix.graphql.dgs.client.codegen.exampleprojection.EntitiesProjectionRoot
import graphql.language.OperationDefinition
import graphql.language.StringValue
import graphql.language.Value
import graphql.parser.InvalidSyntaxException
import graphql.parser.Parser
import graphql.schema.Coercing
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

class GraphQLQueryRequestTest {
    @Test
    fun testSerializeListOfStringsAsInput() {
        val query = TestGraphQLQuery().apply {
            input["actors"] = "actorA"
            input["movies"] = listOf("movie1", "movie2")
        }
        val request = GraphQLQueryRequest(query)
        val result = request.serialize()
        assertValidQuery(result)
        assertThat(result).isEqualTo(
            """query {
            |  test(actors: "actorA", movies: ["movie1", "movie2"])
            |}
            """.trimMargin()
        )
    }

    @Test
    fun testSerializeListOfIntegersAsInput() {
        val query = TestGraphQLQuery().apply {
            input["movies"] = listOf(1234, 5678)
        }
        val request = GraphQLQueryRequest(query)
        val result = request.serialize()
        assertValidQuery(result)
        assertThat(result).isEqualTo(
            """query {
            |  test(movies: [1234, 5678])
            |}
            """.trimMargin()
        )
    }

    @Test
    fun testSerializeInputWithMultipleParameters() {
        val query = TestGraphQLQuery().apply {
            input["name"] = "noname"
            input["age"] = 30
        }
        val request = GraphQLQueryRequest(query)
        val result = request.serialize()
        assertValidQuery(result)
        assertThat(result).isEqualTo(
            """query {
            |  test(name: "noname", age: 30)
            |}
            """.trimMargin()
        )
    }

    @Test
    fun testSerializeInputClass() {
        val query = TestGraphQLQuery().apply {
            input["movie"] = Movie(1234, "testMovie")
        }
        val request = GraphQLQueryRequest(query)
        val result = request.serialize()
        assertValidQuery(result)
        assertThat(result).isEqualTo(
            """query {
            |  test(movie: {movieId : 1234, name : "testMovie"})
            |}
            """.trimMargin()
        )
    }

    @Test
    fun testSerializeInputClassWithProjection() {
        val query = TestGraphQLQuery().apply {
            input["movie"] = Movie(1234, "testMovie")
        }
        val request = GraphQLQueryRequest(query, MovieProjection().name().movieId())
        val result = request.serialize()
        assertValidQuery(result)
        assertThat(result).isEqualTo(
            """query {
            |  test(movie: {movieId : 1234, name : "testMovie"}) {
            |    name
            |    movieId
            |  }
            |}
            """.trimMargin()
        )
    }

    @Test
    fun testSerializeMutation() {
        val query = TestGraphQLMutation().apply {
            input["movie"] = Movie(1234, "testMovie")
        }
        val request = GraphQLQueryRequest(query, MovieProjection().name().movieId())
        val result = request.serialize()
        assertValidQuery(result)
        assertThat(result).isEqualTo(
            """mutation {
            |  testMutation(movie: {movieId : 1234, name : "testMovie"}) {
            |    name
            |    movieId
            |  }
            |}
            """.trimMargin()
        )
    }

    @Test
    fun serializeWithName() {
        val query = TestNamedGraphQLQuery().apply {
            input["movie"] = Movie(123, "greatMovie")
        }
        val request = GraphQLQueryRequest(query, MovieProjection().name().movieId())
        val result = request.serialize()
        assertValidQuery(result)
        assertThat(result).isEqualTo(
            """query TestNamedQuery {
            |  test(movie: {movieId : 123, name : "greatMovie"}) {
            |    name
            |    movieId
            |  }
            |}
            """.trimMargin()
        )
    }

    @Test
    fun serializeWithScalar() {
        val query = TestNamedGraphQLQuery().apply {
            input["movie"] = Movie(123, "greatMovie")
            input["dateRange"] = DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 5, 11))
        }
        val request =
            GraphQLQueryRequest(query, MovieProjection(), mapOf(DateRange::class.java to DateRangeScalar()))

        val result = request.serialize()
        assertValidQuery(result)
        assertThat(result).isEqualTo(
            """query TestNamedQuery {
            |  test(movie: {movieId : 123, name : "greatMovie"}, dateRange: "01/01/2020-05/11/2021")
            |}
            """.trimMargin()
        )
    }

    @Test
    fun `serialize with UUID scalar - #416`() {
        val uuidCoercing = object : Coercing<UUID, String> {
            override fun serialize(uuid: Any): String {
                return uuid.toString()
            }

            override fun parseValue(input: Any): UUID {
                return UUID.fromString(input.toString())
            }

            override fun parseLiteral(input: Any): UUID {
                return UUID.fromString(input.toString())
            }

            override fun valueToLiteral(input: Any): Value<*> {
                return StringValue.of(serialize(input))
            }
        }
        val randomUUID = UUID.randomUUID()
        val query = TestNamedGraphQLQuery().apply {
            input["id"] = randomUUID
        }

        val request = GraphQLQueryRequest(query, MovieProjection(), mapOf(UUID::class.java to uuidCoercing))

        val result = request.serialize()
        assertValidQuery(result)
        assertThat(result).isEqualTo(
            """query TestNamedQuery {
            |  test(id: "$randomUUID")
            |}
            """.trimMargin()
        )
    }

    @Test
    fun testQueryWithInlineFragment() {
        val query = TestNamedGraphQLQuery()
        val projection = EntitiesProjectionRoot().onMovie(Optional.of("Movie"))
            .moveId().title().releaseYear()
            .reviews(username = "Foo", score = 10).username().score()
            .root()
        val request = GraphQLQueryRequest(query, projection)

        val serialized = request.serialize()
        assertValidQuery(serialized)
        assertThat(serialized).isEqualTo(
            """query TestNamedQuery {
              |  test {
              |    ... on Movie {
              |      __typename
              |      moveId
              |      title
              |      releaseYear
              |      reviews(username: "Foo", score: 10) {
              |        username
              |        score
              |      }
              |    }
              |  }
              |}
            """.trimMargin()
        )
    }

    @Test
    fun testQueryFieldWithEmptyProjectionAndInputArguments() {
        val query = TestNamedGraphQLQuery()
        data class TitleFormat(val uppercase: Boolean)
        val projection = object : BaseProjectionNode() {
            init {
                fields["movieId"] = null
                fields["title"] = object : BaseProjectionNode() {}
                inputArguments["title"] = listOf(InputArgument(name = "format", value = TitleFormat(true)))
            }
        }
        val request = GraphQLQueryRequest(query, projection)

        val serialized = request.serialize()
        assertValidQuery(serialized)
        assertThat(serialized).isEqualTo(
            """query TestNamedQuery {
              |  test {
              |    movieId
              |    title(format: {uppercase : true})
              |  }
              |}
            """.trimMargin()
        )
    }

    @Test
    fun serializeWithNestedScalar() {
        val query = TestNamedGraphQLQuery().apply {
            input["movie"] = Movie(123, "greatMovie", DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 5, 11)))
        }
        val request =
            GraphQLQueryRequest(query, MovieProjection(), mapOf(DateRange::class.java to DateRangeScalar()))

        val result = request.serialize()
        assertValidQuery(result)
        assertThat(result).isEqualTo(
            """query TestNamedQuery {
            |  test(movie: {movieId : 123, name : "greatMovie", window : "01/01/2020-05/11/2021"})
            |}
            """.trimMargin()
        )
    }

    @Test
    fun testSerializeMapAsInput() {
        val query = TestGraphQLQuery().apply {
            input["actors"] = mapOf("name" to "actorA", "movies" to listOf("movie1", "movie2"))
            input["movie"] = Movie(123, "greatMovie", DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 5, 11)))
        }
        val request = GraphQLQueryRequest(query, MovieProjection(), mapOf(DateRange::class.java to DateRangeScalar()))
        val result = request.serialize()

        assertValidQuery(result)
        assertThat(result).isEqualTo(
            """query {
            |  test(actors: {name : "actorA", movies : ["movie1", "movie2"]}, movie: {movieId : 123, name : "greatMovie", window : "01/01/2020-05/11/2021"})
            |}
            """.trimMargin()
        )
    }

    @Test
    fun testEntitiesQuery() {
        val query = EntitiesGraphQLQuery.Builder()
            .addRepresentationAsVariable(mapOf("__typename" to "Movie", "id" to 1234))
            .build()
        val projection = EntitiesProjectionRoot().onMovie(Optional.of("Movie"))
            .moveId().title().releaseYear()
            .root()
        val request = GraphQLQueryRequest(query, projection)
        val serialized = request.serialize()

        assertValidQuery(serialized)
        assertThat(serialized).isEqualTo(
            """query (${'$'}representations: [_Any!]!) {
              |  _entities(representations: ${'$'}representations) {
              |    ... on Movie {
              |      __typename
              |      moveId
              |      title
              |      releaseYear
              |    }
              |  }
              |}
            """.trimMargin()
        )
    }

    /**
     * Assert that the GraphQL query is syntactically valid.
     */
    private fun assertValidQuery(query: String) {
        val doc = try {
            Parser().parseDocument(query)
        } catch (exc: InvalidSyntaxException) {
            throw AssertionError("The query failed to parse: ${exc.localizedMessage}")
        }
        doc.getFirstDefinitionOfType(OperationDefinition::class.java)
            .orElseThrow { AssertionError("No operation definition found in document") }
    }
}

class TestGraphQLQuery : GraphQLQuery() {
    override fun getOperationName(): String {
        return "test"
    }
}

class TestNamedGraphQLQuery : GraphQLQuery("query", "TestNamedQuery") {
    override fun getOperationName(): String {
        return "test"
    }
}

class TestGraphQLMutation : GraphQLQuery("mutation") {
    override fun getOperationName(): String {
        return "testMutation"
    }
}

data class Movie(val movieId: Int, val name: String, val window: DateRange? = null)

class MovieProjection : BaseProjectionNode() {
    fun movieId(): MovieProjection {
        fields["movieId"] = null
        return this
    }

    fun name(): MovieProjection {
        fields["name"] = null
        return this
    }
}
