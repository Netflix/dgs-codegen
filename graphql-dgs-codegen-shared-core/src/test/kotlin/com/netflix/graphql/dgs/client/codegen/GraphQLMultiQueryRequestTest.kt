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

import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest.GraphQLQueryRequestOptions
import graphql.language.Argument
import graphql.language.BooleanValue
import graphql.language.Directive
import graphql.language.Field
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.SelectionSet
import graphql.language.StringValue
import graphql.language.TypeName
import graphql.language.VariableDefinition
import graphql.language.VariableReference
import graphql.parser.Parser
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.validation.Validator
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.Locale

class GraphQLMultiQueryRequestTest {
    @ParameterizedTest
    @ValueSource(strings = ["query", "mutation"])
    fun testPreservesRootFieldDirectivesForEachRequest(operation: String) {
        val directives = mutableListOf(Directive("include", listOf(Argument("if", VariableReference.of("enabled")))))
        val options = GraphQLQueryRequestOptions().apply { operationFieldDirectives = directives }
        val firstQuery =
            object : GraphQLQuery(operation) {
                override fun getOperationName(): String = "test"
            }.apply {
                queryAlias = "first"
                variableDefinitions.add(VariableDefinition("enabled", NonNullType(TypeName("Boolean"))))
            }
        val firstRequest = GraphQLQueryRequest(firstQuery, MovieProjection().name(), options)
        directives.clear()
        options.operationFieldDirectives = listOf(Directive("skip", listOf(Argument("if", BooleanValue(false)))))
        val secondQuery =
            object : GraphQLQuery(operation) {
                override fun getOperationName(): String = "test"
            }.apply { queryAlias = "second" }
        val secondRequest = GraphQLQueryRequest(secondQuery, MovieProjection().name(), options)
        val schema =
            SchemaGenerator().makeExecutableSchema(
                SchemaParser().parse("type Query { test: Movie } type Mutation { test: Movie } type Movie { name: String }"),
                RuntimeWiring.newRuntimeWiring().build(),
            )

        val serialized = GraphQLMultiQueryRequest(listOf(firstRequest, secondRequest)).serialize()

        Assertions.assertThat(Validator().validateDocument(schema, Parser().parseDocument(serialized), Locale.ROOT)).isEmpty()
        Assertions.assertThat(serialized).isEqualTo(
            """$operation (${'$'}enabled: Boolean!) {
                |  first: test @include(if: ${'$'}enabled) {
                |    name
                |  }
                |  second: test @skip(if: false) {
                |    name
                |  }
                |}
            """.trimMargin(),
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["query", "mutation"])
    fun testPreservesExplicitSelectionSetsForEachRequest(operation: String) {
        val requests =
            listOf("name", "movieId").map { field ->
                val query =
                    object : GraphQLQuery(operation, "Combined") {
                        override fun getOperationName(): String = "test"
                    }.apply {
                        queryAlias = field
                        input["actors"] = "actor"
                    }
                val selectionSet = SelectionSet.newSelectionSet().selection(Field.newField(field).build()).build()
                GraphQLQueryRequest(query, selectionSet)
            }
        val schema =
            SchemaGenerator().makeExecutableSchema(
                SchemaParser().parse(
                    """
                    type Query { test(actors: String): Movie }
                    type Mutation { test(actors: String): Movie }
                    type Movie { name: String movieId: Int }
                    """.trimIndent(),
                ),
                RuntimeWiring.newRuntimeWiring().build(),
            )

        val serialized = GraphQLMultiQueryRequest(requests).serialize()

        Assertions.assertThat(Validator().validateDocument(schema, Parser().parseDocument(serialized), Locale.ROOT)).isEmpty()
        Assertions.assertThat(serialized).isEqualTo(
            """$operation Combined {
                |  name: test(actors: "actor") {
                |    name
                |  }
                |  movieId: test(actors: "actor") {
                |    movieId
                |  }
                |}
            """.trimMargin(),
        )
    }

    @Test
    fun testSerializeVariableReferencesWithMultipleQueries() {
        val booksQuery =
            object : GraphQLQuery() {
                override fun getOperationName(): String = "books"
            }.apply {
                input["author"] = ""
                variableReferences["author"] = "authorName"
                variableDefinitions.add(VariableDefinition("authorName", TypeName("String")))
            }
        val authorsQuery =
            object : GraphQLQuery() {
                override fun getOperationName(): String = "authors"
            }
        val booksRequest = GraphQLQueryRequest(booksQuery, MovieProjection().name())
        val authorsRequest = GraphQLQueryRequest(authorsQuery, MovieProjection().name())

        Assertions.assertThat(booksRequest.serialize()).contains("author: \$authorName", "\$authorName: String")
        Assertions.assertThat(GraphQLMultiQueryRequest(listOf(booksRequest, authorsRequest)).serialize()).isEqualTo(
            """query (${'$'}authorName: String) {
                |  books(author: ${'$'}authorName) {
                |    name
                |  }
                |  authors {
                |    name
                |  }
                |}
            """.trimMargin(),
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["query", "mutation"])
    fun testSerializeVariablesFromEveryRequestAndProjection(operation: String) {
        val firstQuery =
            object : GraphQLQuery(operation, "Combined") {
                override fun getOperationName(): String = "test"
            }.apply {
                queryAlias = "first"
                input["actors"] = ""
                input["movies"] = listOf("literal")
                variableReferences["actors"] = "actor"
                variableDefinitions.add(VariableDefinition("actor", NonNullType(TypeName("String"))))
            }
        val secondQuery =
            object : GraphQLQuery(operation) {
                override fun getOperationName(): String = "test"
            }.apply {
                queryAlias = "second"
                input["movies"] = emptyList<Int>()
                variableReferences["movies"] = "movieIds"
                variableDefinitions.add(VariableDefinition("movieIds", NonNullType(ListType(NonNullType(TypeName("Int"))))))
            }
        val projection =
            MovieProjection().name().apply {
                inputArguments["name"] = listOf(BaseProjectionNode.InputArgument("locale", "locale", true, TypeName("String")))
            }
        val request =
            GraphQLMultiQueryRequest(
                listOf(GraphQLQueryRequest(firstQuery, projection), GraphQLQueryRequest(secondQuery)),
            )

        Assertions.assertThat(request.serialize()).isEqualTo(
            """$operation Combined(${'$'}actor: String!, ${'$'}locale: String, ${'$'}movieIds: [Int!]!) {
                |  first: test(actors: ${'$'}actor, movies: ["literal"]) {
                |    name(locale: ${'$'}locale)
                |  }
                |  second: test(movies: ${'$'}movieIds)
                |}
            """.trimMargin(),
        )
    }

    @Test
    fun testSharedVariableBetweenRootAndProjectionAcrossSerializations() {
        val query =
            TestGraphQLQuery().apply {
                input["actors"] = ""
                variableReferences["actors"] = "shared"
                variableDefinitions.add(VariableDefinition("shared", NonNullType(TypeName("String"))))
            }
        val projection =
            MovieProjection().name().apply {
                inputArguments["name"] =
                    listOf(BaseProjectionNode.InputArgument("locale", "shared", true, NonNullType(TypeName("String"))))
            }
        val request = GraphQLMultiQueryRequest(listOf(GraphQLQueryRequest(query, projection)))
        val expected =
            """query (${'$'}shared: String!) {
                |  test(actors: ${'$'}shared) {
                |    name(locale: ${'$'}shared)
                |  }
                |}
            """.trimMargin()

        Assertions.assertThat(request.serialize()).isEqualTo(expected)
        Assertions.assertThat(request.serialize()).isEqualTo(expected)
    }

    @ParameterizedTest
    @CsvSource("false, false", "false, true", "true, false", "true, true")
    fun testReusedQueryDropsObsoleteProjectionVariables(
        serializeMultiQueryFirst: Boolean,
        reuseProjection: Boolean,
    ) {
        val query =
            TestGraphQLQuery().apply {
                input["actors"] = ""
                variableReferences["actors"] = "actor"
                variableDefinitions.add(VariableDefinition("actor", TypeName("String")))
            }
        val projection =
            MovieProjection().name().apply {
                inputArguments["name"] = listOf(BaseProjectionNode.InputArgument("locale", "locale", true, TypeName("String")))
            }
        val request = GraphQLQueryRequest(query, projection)
        val multiRequest = GraphQLMultiQueryRequest(listOf(request))
        val schema =
            SchemaGenerator().makeExecutableSchema(
                SchemaParser().parse("type Query { test(actors: String): Movie } type Movie { name(locale: String): String }"),
                RuntimeWiring.newRuntimeWiring().build(),
            )
        val first = if (serializeMultiQueryFirst) multiRequest.serialize() else request.serialize()
        Assertions.assertThat(Validator().validateDocument(schema, Parser().parseDocument(first), Locale.ROOT)).isEmpty()
        Assertions.assertThat(first).contains("\$locale: String", "locale: \$locale")

        val literalProjection = if (reuseProjection) projection else MovieProjection().name()
        literalProjection.inputArguments["name"] = listOf(BaseProjectionNode.InputArgument("locale", "en"))
        val literalRequest = if (reuseProjection) request else GraphQLQueryRequest(query, literalProjection)
        val literalMultiRequest = if (reuseProjection) multiRequest else GraphQLMultiQueryRequest(listOf(literalRequest))

        for (serialized in listOf(literalMultiRequest.serialize(), literalRequest.serialize(), literalRequest.serializeCompact())) {
            Assertions.assertThat(Validator().validateDocument(schema, Parser().parseDocument(serialized), Locale.ROOT)).isEmpty()
            Assertions.assertThat(serialized).contains("\$actor").doesNotContain("\$locale")
        }
    }

    @ParameterizedTest
    @CsvSource("false, false", "true, false", "false, true", "true, true")
    fun testSharedVariableDefaultsRemainUnambiguous(
        conflictingDefaults: Boolean,
        differingNullability: Boolean,
    ) {
        val requests =
            listOf("first", "second").map { alias ->
                val query =
                    TestGraphQLQuery().apply {
                        queryAlias = alias
                        input["actors"] = ""
                        variableReferences["actors"] = "shared"
                        variableDefinitions.add(
                            VariableDefinition
                                .newVariableDefinition(
                                    "shared",
                                    if (differingNullability && alias == "second") NonNullType(TypeName("String")) else TypeName("String"),
                                ).defaultValue(StringValue(if (conflictingDefaults) alias else "default"))
                                .build(),
                        )
                    }
                GraphQLQueryRequest(query)
            }
        val schema =
            SchemaGenerator().makeExecutableSchema(
                SchemaParser().parse("type Query { test(actors: String): String }"),
                RuntimeWiring.newRuntimeWiring().build(),
            )
        val serialized = GraphQLMultiQueryRequest(requests).serialize()
        val errors = Validator().validateDocument(schema, Parser().parseDocument(serialized), Locale.ROOT)

        if (conflictingDefaults) {
            Assertions.assertThat(errors).isNotEmpty()
        } else {
            Assertions.assertThat(errors).isEmpty()
            val expectedType = if (differingNullability) "String!" else "String"
            Assertions.assertThat(serialized).contains("\$shared: $expectedType = \"default\"")
        }
    }

    @ParameterizedTest
    @CsvSource(
        "String, String!, String!",
        "String!, String, String!",
        "[String], [String!]!, [String!]!",
        "[String!]!, [String], [String!]!",
        "[String!], [String]!, [String!]!",
        "[[String]], [[String!]!]!, [[String!]!]!",
    )
    fun testSharedVariableUsesStrictestNullability(
        firstType: String,
        secondType: String,
        expectedType: String,
    ) {
        val requests =
            listOf("first" to firstType, "second" to secondType).map { (field, type) ->
                val query =
                    object : GraphQLQuery() {
                        override fun getOperationName(): String = field
                    }.apply {
                        input["value"] = ""
                        variableReferences["value"] = "shared"
                        variableDefinitions.add(VariableDefinition("shared", Parser.parseType(type)))
                    }
                GraphQLQueryRequest(query)
            }
        val schema =
            SchemaGenerator().makeExecutableSchema(
                SchemaParser().parse("type Query { first(value: $firstType): String second(value: $secondType): String }"),
                RuntimeWiring.newRuntimeWiring().build(),
            )
        val serialized = GraphQLMultiQueryRequest(requests).serialize()

        Assertions.assertThat(Validator().validateDocument(schema, Parser().parseDocument(serialized), Locale.ROOT)).isEmpty()
        Assertions.assertThat(serialized).contains("query (\$shared: $expectedType)")
    }

    @ParameterizedTest
    @CsvSource("String, ID", "String, [String]", "[String], [[String]]")
    fun testConflictingSharedVariableTypesRemainInvalid(
        firstType: String,
        secondType: String,
    ) {
        val requests =
            listOf("first" to firstType, "second" to secondType).map { (field, type) ->
                val query =
                    object : GraphQLQuery() {
                        override fun getOperationName(): String = field
                    }.apply {
                        input["value"] = ""
                        variableReferences["value"] = "shared"
                        variableDefinitions.add(VariableDefinition("shared", Parser.parseType(type)))
                    }
                GraphQLQueryRequest(query)
            }
        val schema =
            SchemaGenerator().makeExecutableSchema(
                SchemaParser().parse("type Query { first(value: $firstType): String second(value: $secondType): String }"),
                RuntimeWiring.newRuntimeWiring().build(),
            )
        val serialized = GraphQLMultiQueryRequest(requests).serialize()

        Assertions.assertThat(Validator().validateDocument(schema, Parser().parseDocument(serialized), Locale.ROOT)).isNotEmpty()
        Assertions.assertThat(serialized).contains("\$shared: $firstType", "\$shared: $secondType")
    }

    @Test
    fun testSerializeInputClassWithProjectionAndMultipleQueries() {
        val query =
            TestGraphQLQuery().apply {
                input["movie"] = Movie(1234, "testMovie")
            }
        val query2 =
            TestGraphQLQuery().apply {
                input["actors"] = "actorA"
                input["movies"] = listOf("movie1", "movie2")
            }

        query.queryAlias = "alias1"
        query2.queryAlias = "alias2"

        val multiRequest =
            GraphQLMultiQueryRequest(
                listOf(
                    GraphQLQueryRequest(query, MovieProjection().name().movieId()),
                    GraphQLQueryRequest(query2, MovieProjection().name()),
                ),
            )

        val result = multiRequest.serialize()
        GraphQLQueryRequestTest.assertValidQuery(result)
        Assertions.assertThat(result).isEqualTo(
            """{
                |  alias1: test(movie: {movieId : 1234, name : "testMovie"}) {
                |    name
                |    movieId
                |  }
                |  alias2: test(actors: "actorA", movies: ["movie1", "movie2"]) {
                |    name
                |  }
                |}
            """.trimMargin(),
        )
    }

    @Test
    fun testSerializeInputClassWithProjectionAndMultipleMutations() {
        val query =
            TestGraphQLMutation().apply {
                input["movie"] = Movie(1234, "testMovie")
            }
        val query2 =
            TestGraphQLMutation().apply {
                input["actors"] = "actorA"
                input["movies"] = listOf("movie1", "movie2")
            }
        val query3 =
            TestGraphQLMutation().apply {
                input["actors"] = "actorA"
                input["movies"] = listOf("movie1", "movie2")
            }

        query.queryAlias = "alias1"
        query2.queryAlias = "alias2"
        query3.queryAlias = "alias3"

        val multiRequest =
            GraphQLMultiQueryRequest(
                listOf(
                    GraphQLQueryRequest(query),
                    GraphQLQueryRequest(query2),
                    GraphQLQueryRequest(query3),
                ),
            )

        val result = multiRequest.serialize()
        GraphQLQueryRequestTest.assertValidQuery(result)
        Assertions.assertThat(result).isEqualTo(
            """mutation {
              |  alias1: testMutation(movie: {movieId : 1234, name : "testMovie"})
              |  alias2: testMutation(actors: "actorA", movies: ["movie1", "movie2"])
              |  alias3: testMutation(actors: "actorA", movies: ["movie1", "movie2"])
              |}
            """.trimMargin(),
        )
    }

    @Test
    fun testSerializeInputClassWithProjectionAndMultipleMutations_MismatchOperationType() {
        val query =
            TestGraphQLMutation().apply {
                input["movie"] = Movie(1234, "testMovie")
            }

        val query2 =
            TestGraphQLQuery().apply {
                input["actors"] = "actorA"
                input["movies"] = listOf("movie1", "movie2")
            }

        val multiRequest =
            GraphQLMultiQueryRequest(
                listOf(
                    GraphQLQueryRequest(query),
                    GraphQLQueryRequest(query2),
                ),
            )

        assertThrows<AssertionError> {
            multiRequest.serialize()
        }
    }

    @Test
    fun testSerializeInputClassWithProjectionAndSingleQueriesAndAlias() {
        val query =
            TestGraphQLQuery().apply {
                input["movie"] = Movie(1234, "testMovie")
            }

        query.queryAlias = "alias1"

        val multiRequest =
            GraphQLMultiQueryRequest(
                listOf(
                    GraphQLQueryRequest(query, MovieProjection().name().movieId()),
                ),
            )

        val result = multiRequest.serialize()
        GraphQLQueryRequestTest.assertValidQuery(result)
        Assertions.assertThat(result).isEqualTo(
            """{
                |  alias1: test(movie: {movieId : 1234, name : "testMovie"}) {
                |    name
                |    movieId
                |  }
                |}
            """.trimMargin(),
        )
    }
}
