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

import com.google.common.truth.Truth
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.ParameterizedTypeName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClientApiGenTest {

    val basePackageName = "com.netflix.graphql.dgs.codegen.tests.generated"

    @Test
    fun generateQueryType() {

        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("PeopleGraphQLQuery")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @Test
    fun generateMutationType() {

        val schema = """
            type Mutation {
                updateMovie(movieId: ID, title: String): Movie
            }
            
            type Movie {
                movieId: ID
                title: String
            }
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("UpdateMovieGraphQLQuery")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @Test
    fun generateMutationWithInputType() {

        val schema = """
            type Mutation {
                updateMovie(movie: MovieDescription): Movie
            }
            
            input MovieDescription {
                movieId: ID
                title: String
                actors: [String]
            }
            
            type Movie {
                movieId: ID
                lastname: String
            }
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("UpdateMovieGraphQLQuery")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes))
    }

    @Test
    fun generateMutationAddsNullChecksDuringInit() {

        val schema = """
            type Mutation {
                updateMovie(movie: MovieDescription): Movie
            }
            
            input MovieDescription {
                movieId: ID
                title: String
                actors: [String]
            }
            
            type Movie {
                movieId: ID
                lastname: String
            }
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult


        assert(codeGenResult.queryTypes[0].typeSpec.methodSpecs
                .find { it.name == "<init>" }?.code.toString()
                .contains("super(\"mutation\");\nif (movie != null) {\n    getInput().put(\"movie\", movie);\n}"))

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes))
    }

    @Test
    fun generateProjectionRoot() {

        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(1)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PeopleProjectionRoot")

        assertCompiles(codeGenResult.clientProjections)
    }

    @Test
    fun generateProjectionRootTestWithCycles() {

        val schema = """
            type Query @extends {
                persons: [Person]
            }

            type Person {
             name: String
             friends: [Person]
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult
        assertThat(codeGenResult.clientProjections.size).isEqualTo(2)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PersonsProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("PersonsFriendsProjection")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("name")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("friends")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("name")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @Test
    fun generateInterfaceProjectionsWithCycles() {
        val schema = """
            type Query {
                search(title: String): [Show]
            }
            
            interface Show {
                title: String
            }
            
            type Movie implements Show {
                title: String
                duration: Int
                details: Details
            }
            
            type Details {
                 show: Show
            }
            
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(5)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("SearchMovieProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("SearchMovieDetailsProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("SearchMovieDetailsShowProjection")
        assertThat(codeGenResult.clientProjections[4].typeSpec.name).isEqualTo("SearchMovieDetailsShowMovieProjection")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.interfaces))
    }

    @Test
    fun generateUnionProjectionsWithCycles() {
        val schema = """
            type Query {
                search(title: String): [Video]
            }
            
            union Video = Show | Movie
            
            type Show {
                title: String
            }
            
            type Movie {
                title: String
                duration: Int
                related: Related
            }
            
            type Related {
                 video: Video
            }
            
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(7)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("SearchShowProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("SearchMovieProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("SearchMovieRelatedProjection")
        assertThat(codeGenResult.clientProjections[4].typeSpec.name).isEqualTo("SearchMovieRelatedVideoProjection")
        assertThat(codeGenResult.clientProjections[5].typeSpec.name).isEqualTo("SearchMovieRelatedVideoShowProjection")
        assertThat(codeGenResult.clientProjections[6].typeSpec.name).isEqualTo("SearchMovieRelatedVideoMovieProjection")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.interfaces))
    }

    @Test
    fun generateSubProjectionsWithDifferentRootTypes() {

        val schema = """
            type Query @extends {
                persons: [Person]
                friends: [Person]
            }

            type Person {
             name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PersonsProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("FriendsProjectionRoot")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @Test
    fun generateSubProjectionsWithDifferentParentTypes() {

        val schema = """
            type Query @extends {
                persons: [Person]
                details(name: String): Details
            }

            type Person {
               details: Details
            }
            
            type Details {
                name: String
                age: Integer
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PersonsProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("PersonsDetailsProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("DetailsProjectionRoot")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @Test
    fun generateSubProjectionTypes() {

        val schema = """
            type Query {
                movies: [Movie]
            }
            
            type Movie {
                title: String
                actors: [Actor]
            }
            
            type Actor {
                name: String
                age: Integer
            }

        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(2)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("MoviesProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("MoviesActorsProjection")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @Test
    fun generateSubProjectionTypesWithShortNames() {

        val schema = """
            type Query {
                movies: [Movie]
            }
            
            type Movie {
                title: String
                actors: [Actor]
            }
            
            type Actor {
                name: String
                age: Integer
                movies: [Movie]
            }

        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                shortProjectionNames = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("MoviesProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("MoviesActorsProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("MoAcMoviesProjection")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @Test
    fun generateArgumentsForSimpleTypes() {

        val schema = """
            type Query {
                personSearch(lastname: String): [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }

        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult
        assertThat(codeGenResult.queryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].name).isEqualTo("lastname")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @Test
    fun generateArgumentsForEnum() {

        val schema = """
            type Query {
                personSearch(index: SearchIndex): [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
            
            enum SearchIndex {
                TEST, PROD
            }
            
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].name).isEqualTo("index")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes))
    }

    @Test
    fun generateArgumentsForObjectType() {

        val schema = """
            type Query {
                personSearch(index: SearchIndex): [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
            
            type SearchIndex {
                name: String
            }
            
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("PersonSearchGraphQLQuery")
        assertThat(codeGenResult.queryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].name).isEqualTo("index")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes))
    }

    @Test
    fun skipCodegen() {

        val schema = """
            type Query {
                persons: [Person]
                personSearch(index: SearchIndex): [Person] @skipcodegen
            }
            
            type Person {
                firstname: String
                lastname: String
            }
            
            type SearchIndex {
                name: String
            }
            
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("PersonsGraphQLQuery")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes))
    }

    @Test
    fun interfaceReturnTypes() {
        val schema = """
            type Query {
                search(title: String): [Show]
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

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("SearchGraphQLQuery")
        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs[0].name).isEqualTo("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("SearchMovieProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs[1].name).isEqualTo("duration")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("SearchSeriesProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs[1].name).isEqualTo("episodes")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.interfaces))
    }

    @Test
    fun interfaceFragment() {
        val schema = """
            type Query {
                search(title: String): [Show]
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

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs[0].name).isEqualTo("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("SearchMovieProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("duration")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("episodes")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("SearchSeriesProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("episodes")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("title")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("duration")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.interfaces))
    }

    @Test
    fun interfaceFragmentOnSubType() {
        val schema = """
            type Query {
                search(title: String): [Result]
            }
            
            type Result {
                show: Show
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

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(4)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("SearchShowProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("SearchShowMovieProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("duration")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("title")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("episodes")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("SearchShowSeriesProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").contains("episodes")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").doesNotContain("title")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").doesNotContain("duration")

        val superclass = codeGenResult.clientProjections[3].typeSpec.superclass as ParameterizedTypeName
        assertThat(superclass.typeArguments[1]).extracting("simpleName").containsExactly("SearchProjectionRoot")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.interfaces))
    }

    @Test
    fun unionFragment() {
        val schema = """
            type Query {
                search: [Result]
            }
            
            union Result = Movie | Actor

            type Movie {
                title: String
            }

            type Actor {
                name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("onMovie")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("onActor")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("SearchMovieProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("name")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("SearchActorProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("name")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("title")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.interfaces))
    }

    @Test
    fun unionFragmentOnSubType() {
        val schema = """
            type Query {
                search(title: String): [Result]
            }
            
            type Result {
                result: SearchResult
            }
            
            union SearchResult = Movie | Actor

            type Movie {
                title: String
            }

            type Actor {
                name: String
            }
            
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(4)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("SearchResultProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("name")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("onMovie")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("onActor")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("SearchResultMovieProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("name")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("SearchResultActorProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").contains("name")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").doesNotContain("title")

        assertThat(codeGenResult.clientProjections[2].typeSpec.initializerBlock.isEmpty).isFalse()
        assertThat(codeGenResult.clientProjections[3].typeSpec.initializerBlock.isEmpty).isFalse()

        val superclass = codeGenResult.clientProjections[3].typeSpec.superclass as ParameterizedTypeName
        assertThat(superclass.typeArguments[1]).extracting("simpleName").containsExactly("SearchProjectionRoot")

        val searchResult = codeGenResult.interfaces[0].typeSpec

        Truth.assertThat(JavaFile.builder("$basePackageName.types", searchResult).build().toString()).isEqualTo(
                """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types;
                |
                |import com.fasterxml.jackson.annotation.JsonSubTypes;
                |import com.fasterxml.jackson.annotation.JsonTypeInfo;
                |
                |@JsonTypeInfo(
                |    use = JsonTypeInfo.Id.NAME,
                |    include = JsonTypeInfo.As.PROPERTY,
                |    property = "__typename"
                |)
                |@JsonSubTypes({
                |    @JsonSubTypes.Type(value = Movie.class, name = "Movie"),
                |    @JsonSubTypes.Type(value = Actor.class, name = "Actor")
                |})
                |public interface SearchResult {
                |}
                |""".trimMargin())

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.interfaces))
    }

    @Test
    fun testScalarsDontGenerateProjections() {
        val schema = """
          type Query {
              movieCountry: MovieCountry
          }
          
          type MovieCountry {
            country: String
            movieId: Long
          }
          scalar Long

        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                typeMapping = mapOf(Pair("Long", "java.lang.Long")),
        )).generate() as CodeGenResult
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(1)
        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.enumTypes))
    }

    @Test
    fun testExtendRootProjection() {
        val schema = """
          type Query {
              people: [Person]
          }
          
          type Person {
            name: String
          }
          
          extend type Person {
            email: String
          }

        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                typeMapping = mapOf(Pair("Long", "java.lang.Long")),
        )).generate() as CodeGenResult
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(1)
        assertThat(projections[0].typeSpec.name).isEqualTo("PeopleProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs.size).isEqualTo(2)
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactly("name", "email")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.enumTypes))
    }

    @Test
    fun testExtendSubProjection() {
        val schema = """
          type Query {
            search: [SearchResult]
          }
          
          type SearchResult {
            movie: Movie
          }
          
          type Movie {
            title: String
          }
          
          extend type Movie {
            director: String
          }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                typeMapping = mapOf(Pair("Long", "java.lang.Long")),
        )).generate() as CodeGenResult
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(2)
        assertThat(projections[1].typeSpec.name).isEqualTo("SearchMovieProjection")
        assertThat(projections[1].typeSpec.methodSpecs.size).isEqualTo(3)
        assertThat(projections[1].typeSpec.methodSpecs).extracting("name").contains("title", "director", "<init>")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.enumTypes))
    }

    @Test
    fun includeQueryConfig() {

        val schema = """
            type Query {
                movieTitles: [String]
                actorNames: [String]
            }           
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                includeQueries = setOf("movieTitles"),
        )).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("MovieTitlesGraphQLQuery")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @Test
    fun includeMutationConfig() {

        val schema = """
            type Mutation {
                updateMovieTitle: String
                addActorName: Boolean
            }           
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                includeMutations = setOf("updateMovieTitle"),
        )).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("UpdateMovieTitleGraphQLQuery")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @Test
    fun generateProjectionRootWithReservedNames() {

        val schema = """
            type Query {
                weirdType: WeirdType
            }
            
            type WeirdType {
                _: String
                root: String
                parent: String
                import: String
            }
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(1)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("WeirdTypeProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").containsExactly("__", "_root", "_parent", "_import")

        assertCompiles(codeGenResult.clientProjections)
    }

    @Test
    fun generateSubProjectionWithReservedNames() {

        val schema = """
            type Query {
                normalType: NormalType
            }
            
            type NormalType {
                weirdType: WeirdType
            }
            
            type WeirdType {
                _: String
                root: String
                parent: String
                import: String
            }
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(2)
        val weirdType = codeGenResult.clientProjections.find { it.typeSpec.name == "NormalTypeWeirdTypeProjection" }

        assertThat(weirdType?.typeSpec?.methodSpecs).extracting("name").contains("__", "_root", "_parent", "_import")

        assertCompiles(codeGenResult.clientProjections)
    }

    @Test
    fun generateProjectionsForSameTypeInSameQueryWithDifferentPaths() {
        val schema = """
            type Query {
                workshop: Workshop
            }
            
            type Workshop {
                reviews: ReviewConnection
                assets: Asset
            }
            
            type ReviewConnection {
                edges: [ReviewEdge]
            }
            
            type ReviewEdge {
                node: String
            }
            
            type Asset {
                reviews: ReviewConnection          
            }                     
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, writeToFiles = false)).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(6)
        val workshopAssetsReviewsProjection = codeGenResult.clientProjections.find { it.typeSpec.name == "WorkshopAssetsReviewsProjection" }!!
        val workshopReviewsProjection = codeGenResult.clientProjections.find { it.typeSpec.name == "WorkshopReviewsProjection" }!!

        assertThat(workshopReviewsProjection.typeSpec.methodSpecs).extracting("name").contains("edges")
        assertThat(workshopAssetsReviewsProjection.typeSpec.methodSpecs).extracting("name").contains("edges")
    }

    @Test
    fun generateOnlyRequiredDataTypesForQuery() {
        val schema = """
            type Query {
                shows(showFilter: ShowFilter): [Show]
                people(personFilter: PersonFilter): [Person]
            }
            
            type Show {
                title: String
            }
            
            enum ShouldNotInclude {
                YES,NO
            }
            
            input NotUsed {
                field: String
            }
            
            input ShowFilter {
                title: String
                showType: ShowType
                similarTo: SimilarityInput              
            }
            
            input SimilarityInput {
                tags: [String]
            }
            
            enum ShowType {
                MOVIE, SERIES
            }
            
            type Person {
                name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, includeQueries = setOf("shows"), generateDataTypes = false, writeToFiles = false)).generate() as CodeGenResult
        assertThat(codeGenResult.dataTypes.size).isEqualTo(2)
        assertThat(codeGenResult.dataTypes).extracting("typeSpec").extracting("name").containsExactly("ShowFilter", "SimilarityInput")
        assertThat(codeGenResult.enumTypes).extracting("typeSpec").extracting("name").containsExactly("ShowType")

        assertCompiles(codeGenResult.clientProjections + codeGenResult.dataTypes + codeGenResult.enumTypes)
    }

    @Test
    fun generateOnlyRequiredDataTypesForMutation() {
        val schema = """
            type Mutation {
                shows(showFilter: ShowFilter): [Show]
                people(personFilter: PersonFilter): [Person]
            }
            
            type Show {
                title: String
            }
            
            enum ShouldNotInclude {
                YES,NO
            }
            
            input NotUsed {
                field: String
            }
            
            input ShowFilter {
                title: String
                showType: ShowType
                similarTo: SimilarityInput              
            }
            
            input SimilarityInput {
                tags: [String]
            }
            
            enum ShowType {
                MOVIE, SERIES
            }
            
            type Person {
                name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, includeMutations = setOf("shows"), generateDataTypes = false, writeToFiles = false)).generate() as CodeGenResult
        assertThat(codeGenResult.dataTypes.size).isEqualTo(2)
        assertThat(codeGenResult.dataTypes).extracting("typeSpec").extracting("name").containsExactly("ShowFilter", "SimilarityInput")
        assertThat(codeGenResult.enumTypes).extracting("typeSpec").extracting("name").containsExactly("ShowType")

        assertCompiles(codeGenResult.clientProjections + codeGenResult.dataTypes + codeGenResult.enumTypes)
    }

    @Test
    fun generateSubProjectionTypesMaxDepth() {

        val schema = """
            type Query {
                movies: [Movie]
            }
            
            type Movie {
                title: String
                actors: [Actor]
            }
            
            type Actor {
                name: String
                age: Integer
                agent: Agent
            }
            
            type Agent {
                name: String                
            }

        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                maxProjectionDepth = 0,
        )).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(2)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("MoviesProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("MoviesActorsProjection")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }
}
