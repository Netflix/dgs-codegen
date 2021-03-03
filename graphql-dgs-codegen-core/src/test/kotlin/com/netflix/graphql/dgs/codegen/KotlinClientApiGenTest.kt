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
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KotlinClientApiGenTest {

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
                language = Language.KOTLIN,
                generateClientApi = true,
        )).generate() as KotlinCodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        val typeSpec = codeGenResult.queryTypes[0].members[0] as TypeSpec
        assertThat(typeSpec.name).isEqualTo("PeopleGraphQLQuery")
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
                language = Language.KOTLIN,
                generateClientApi = true,
        )).generate() as KotlinCodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        val typeSpec = codeGenResult.queryTypes[0].members[0] as TypeSpec
        assertThat(typeSpec.name).isEqualTo("UpdateMovieGraphQLQuery")
    }

    @Test
    fun generateMutationTypeWithInput() {

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
                language = Language.KOTLIN,
                generateClientApi = true,
        )).generate() as KotlinCodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        val typeSpec = codeGenResult.queryTypes[0].members[0] as TypeSpec
        assertThat(typeSpec.name).isEqualTo("UpdateMovieGraphQLQuery")
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
                language = Language.KOTLIN,
                generateClientApi = true,
        )).generate() as KotlinCodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        val typeSpec = codeGenResult.queryTypes[0].members[0] as TypeSpec
        assertThat(typeSpec.name).isEqualTo("SearchGraphQLQuery")
        assertThat(codeGenResult.clientProjections[0].name).isEqualTo("SearchProjectionRoot")
        val projectionType = codeGenResult.clientProjections[0].members[0] as TypeSpec
        assertThat(projectionType.funSpecs[0].name).isEqualTo("title")
        val movieProjectionType = codeGenResult.clientProjections[1].members[0] as TypeSpec
        assertThat(movieProjectionType.name).isEqualTo("SearchMovieProjection")
        val seriesProjectionType = codeGenResult.clientProjections[2].members[0] as TypeSpec
        assertThat(seriesProjectionType.name).isEqualTo("SearchSeriesProjection")
    }

    @Test
    fun interfaceFragments() {

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
                language = Language.KOTLIN,
                generateClientApi = true,
        )).generate() as KotlinCodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        var projectionType = codeGenResult.clientProjections[0].members[0] as TypeSpec
        assertThat(projectionType.name).isEqualTo("SearchProjectionRoot")
        assertThat(projectionType.funSpecs[0].name).isEqualTo("title")
        projectionType = codeGenResult.clientProjections[1].members[0] as TypeSpec
        assertThat(projectionType.name).isEqualTo("SearchMovieProjection")
        assertThat(projectionType.funSpecs).extracting("name").containsExactly("duration", "toString")
        projectionType = codeGenResult.clientProjections[2].members[0] as TypeSpec
        assertThat(projectionType.name).isEqualTo("SearchSeriesProjection")
        assertThat(projectionType.funSpecs).extracting("name").containsExactly("episodes", "toString")
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
                language = Language.KOTLIN,
                generateClientApi = true,
        )).generate() as KotlinCodeGenResult

        var projectionType = codeGenResult.clientProjections[0].members[0] as TypeSpec
        assertThat(projectionType.name).isEqualTo("SearchProjectionRoot")
        projectionType = codeGenResult.clientProjections[1].members[0] as TypeSpec
        assertThat(projectionType.name).isEqualTo("SearchShowProjection")
        projectionType = codeGenResult.clientProjections[2].members[0] as TypeSpec
        assertThat(projectionType.name).isEqualTo("SearchShowMovieProjection")
        projectionType = codeGenResult.clientProjections[3].members[0] as TypeSpec
        assertThat(projectionType.name).isEqualTo("SearchShowSeriesProjection")

        val superclass = projectionType.superclass as ParameterizedTypeName
        assertThat(superclass.typeArguments[1]).extracting("simpleName").containsExactly("SearchProjectionRoot")
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
                language = Language.KOTLIN,
                generateClientApi = true,
        )).generate() as KotlinCodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        val projectionType = codeGenResult.clientProjections[0].members[0] as TypeSpec
        assertThat(projectionType.name).isEqualTo("SearchProjectionRoot")
        assertThat(projectionType.funSpecs).extracting("name").contains("onMovie")
        assertThat(projectionType.funSpecs).extracting("name").contains("onActor")

        val movieProjectionType = codeGenResult.clientProjections[1].members[0] as TypeSpec
        assertThat(movieProjectionType.name).isEqualTo("SearchMovieProjection")
        assertThat(movieProjectionType.funSpecs).extracting("name").contains("title")
        assertThat(movieProjectionType.funSpecs).extracting("name").doesNotContain("name")

        val actorProjectionType = codeGenResult.clientProjections[2].members[0] as TypeSpec
        assertThat(actorProjectionType.name).isEqualTo("SearchActorProjection")
        assertThat(actorProjectionType.funSpecs).extracting("name").contains("name")
        assertThat(actorProjectionType.funSpecs).extracting("name").doesNotContain("title")
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
                language = Language.KOTLIN,
                generateClientApi = true,
        )).generate() as KotlinCodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(4)
        val projectionType = codeGenResult.clientProjections[0].members[0] as TypeSpec
        assertThat(projectionType.name).isEqualTo("SearchProjectionRoot")
        val searchResultProjectionType = codeGenResult.clientProjections[1].members[0] as TypeSpec
        assertThat(searchResultProjectionType.name).isEqualTo("SearchResultProjection")
        assertThat(searchResultProjectionType.funSpecs).extracting("name").contains("onMovie")
        assertThat(searchResultProjectionType.funSpecs).extracting("name").contains("onActor")
        val movieProjectionType = codeGenResult.clientProjections[2].members[0] as TypeSpec
        assertThat(movieProjectionType.name).isEqualTo("SearchResultMovieProjection")
        assertThat(movieProjectionType.funSpecs).extracting("name").contains("title")
        assertThat(movieProjectionType.funSpecs).extracting("name").doesNotContain("name")
        val actorProjectionType = codeGenResult.clientProjections[3].members[0] as TypeSpec
        assertThat(actorProjectionType.name).isEqualTo("SearchResultActorProjection")
        assertThat(actorProjectionType.funSpecs).extracting("name").contains("name")
        assertThat(actorProjectionType.funSpecs).extracting("name").doesNotContain("title")

        assertThat(movieProjectionType.initializerBlock.isNotEmpty()).isTrue()
        assertThat(actorProjectionType.initializerBlock.isNotEmpty()).isTrue()

        val superclass = actorProjectionType.superclass as ParameterizedTypeName
        assertThat(superclass.typeArguments[1]).extracting("simpleName").containsExactly("SearchProjectionRoot")

        val searchResult = codeGenResult.interfaces[0].members[0] as TypeSpec

        Truth.assertThat(FileSpec.get("${basePackageName}.types", searchResult).toString()).isEqualTo(
                """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types
                |
                |import com.fasterxml.jackson.`annotation`.JsonSubTypes
                |import com.fasterxml.jackson.`annotation`.JsonTypeInfo
                |
                |@JsonTypeInfo(
                |  use = JsonTypeInfo.Id.NAME,
                |  include = JsonTypeInfo.As.PROPERTY,
                |  property = "__typename"
                |)
                |@JsonSubTypes(value = [
                |  JsonSubTypes.Type(value = Movie::class, name = "Movie"),
                |  JsonSubTypes.Type(value = Actor::class, name = "Actor")
                |])
                |public interface SearchResult
                |""".trimMargin())
    }

    @Test
    fun skipCodegenOnQuery() {

        val schema = """
            type Query {
                people: [Person] @skipcodegen
                personSearch(name: String): [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                generateClientApi = true,

                )).generate() as KotlinCodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        val typeSpec = codeGenResult.queryTypes[0].members[0] as TypeSpec
        assertThat(typeSpec.name).isEqualTo("PersonSearchGraphQLQuery")
    }

    @Test
    fun skipCodegenField() {
        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String @skipcodegen
                lastname: String
            }
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                generateClientApi = true,

                )).generate() as KotlinCodeGenResult

        val typeSpec = codeGenResult.clientProjections[0].members[0] as TypeSpec
        assertThat(typeSpec.funSpecs.size).isEqualTo(1)
        assertThat(typeSpec.funSpecs[0].name).isEqualTo("lastname")
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
                language = Language.KOTLIN,
                generateClientApi = true,

                )).generate() as KotlinCodeGenResult
        val projectionTypeSpec = codeGenResult.clientProjections[0].members[0] as TypeSpec
        assertThat(codeGenResult.clientProjections.size).isEqualTo(1)
        assertThat(projectionTypeSpec.name).isEqualTo("PeopleProjectionRoot")
    }

    @Test
    fun generateSubProjection() {

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
                language = Language.KOTLIN,
                generateClientApi = true,

                )).generate() as KotlinCodeGenResult
        assertThat(codeGenResult.clientProjections.size).isEqualTo(2)
        val projectionTypeSpec = codeGenResult.clientProjections[0].members[0] as TypeSpec
        assertThat(projectionTypeSpec.name).isEqualTo("MoviesProjectionRoot")
        val actorTypeSpec = codeGenResult.clientProjections[1].members[0] as TypeSpec
        assertThat(actorTypeSpec.name).isEqualTo("MoviesActorsProjection")

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
                language = Language.KOTLIN,
                generateClientApi = true,

                )).generate() as KotlinCodeGenResult
        assertThat(codeGenResult.clientProjections.size).isEqualTo(2)
        var projectionTypeSpec = codeGenResult.clientProjections[0].members[0] as TypeSpec
        assertThat(projectionTypeSpec.name).isEqualTo("PersonsProjectionRoot")
        projectionTypeSpec = codeGenResult.clientProjections[1].members[0] as TypeSpec
        assertThat(projectionTypeSpec.name).isEqualTo("PersonsFriendsProjection")
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
                language = Language.KOTLIN,
                generateClientApi = true,

                )).generate() as KotlinCodeGenResult
        var projectionTypeSpec = codeGenResult.clientProjections[0].members[0] as TypeSpec
        assertThat(projectionTypeSpec.name).isEqualTo("PersonsProjectionRoot")
        projectionTypeSpec = codeGenResult.clientProjections[1].members[0] as TypeSpec
        assertThat(projectionTypeSpec.name).isEqualTo("FriendsProjectionRoot")
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
                language = Language.KOTLIN,
                generateClientApi = true,

                )).generate() as KotlinCodeGenResult
        var projectionTypeSpec = codeGenResult.clientProjections[0].members[0] as TypeSpec
        assertThat(projectionTypeSpec.name).isEqualTo("PersonsProjectionRoot")
        projectionTypeSpec = codeGenResult.clientProjections[1].members[0] as TypeSpec
        assertThat(projectionTypeSpec.name).isEqualTo("PersonsDetailsProjection")
        projectionTypeSpec = codeGenResult.clientProjections[2].members[0] as TypeSpec
        assertThat(projectionTypeSpec.name).isEqualTo("DetailsProjectionRoot")
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
                language = Language.KOTLIN,
                generateClientApi = true,

                )).generate() as KotlinCodeGenResult
        val typeSpec = codeGenResult.queryTypes[0].members[0] as TypeSpec
        assertThat(typeSpec.propertySpecs[0].name).isEqualTo("lastname")
        assertThat(codeGenResult.queryTypes[0].toString()).doesNotContain("import com.netflix.graphql.dgs.codegen.tests.generated.types")

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
                language = Language.KOTLIN,
                generateClientApi = true,

                )).generate() as KotlinCodeGenResult
        val typeSpec = codeGenResult.queryTypes[0].members[0] as TypeSpec
        assertThat(typeSpec.propertySpecs[0].name).isEqualTo("index")
        assertThat(codeGenResult.queryTypes[0].toString()).contains("import com.netflix.graphql.dgs.codegen.tests.generated.types.SearchIndex\n")
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
                language = Language.KOTLIN,
                generateClientApi = true,

                )).generate() as KotlinCodeGenResult
        val typeSpec = codeGenResult.queryTypes[0].members[0] as TypeSpec
        assertThat(typeSpec.propertySpecs[0].name).isEqualTo("index")
        assertThat(codeGenResult.queryTypes[0].toString()).contains("import com.netflix.graphql.dgs.codegen.tests.generated.types.SearchIndex\n")
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
                language = Language.KOTLIN,
                generateClientApi = true,
                typeMapping = mapOf(Pair("Long", "java.lang.Long")),

                )).generate() as KotlinCodeGenResult
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(1)
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
                language = Language.KOTLIN,
                generateClientApi = true,
                typeMapping = mapOf(Pair("Long", "java.lang.Long")),

                )).generate() as KotlinCodeGenResult
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(1)
        assertThat((projections[0].members[0] as TypeSpec).funSpecs).extracting("name").contains("name", "email")
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
                language = Language.KOTLIN,
                generateClientApi = true,
                typeMapping = mapOf(Pair("Long", "java.lang.Long")),

                )).generate() as KotlinCodeGenResult
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(2)
        assertThat((projections[1].members[0] as TypeSpec).funSpecs).extracting("name").contains("title", "director")
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

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, language = Language.KOTLIN)).generate() as KotlinCodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(6)

        val workshopAssetsReviewsProjection = codeGenResult.clientProjections.find { (it.members[0] as TypeSpec).name == "WorkshopAssetsReviewsProjection" }!!
        val workshopReviewsProjection = codeGenResult.clientProjections.find { (it.members[0] as TypeSpec).name == "WorkshopReviewsProjection" }!!

        assertThat((workshopReviewsProjection.members[0] as TypeSpec).funSpecs).extracting("name").contains("edges")
        assertThat((workshopAssetsReviewsProjection.members[0] as TypeSpec).funSpecs).extracting("name").contains("edges")
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

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, language = Language.KOTLIN)).generate() as KotlinCodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(7)
        assertThat((codeGenResult.clientProjections[0].members[0] as TypeSpec).name).isEqualTo("SearchProjectionRoot")
        assertThat((codeGenResult.clientProjections[1].members[0] as TypeSpec).name).isEqualTo("SearchShowProjection")
        assertThat((codeGenResult.clientProjections[2].members[0] as TypeSpec).name).isEqualTo("SearchMovieProjection")
        assertThat((codeGenResult.clientProjections[3].members[0] as TypeSpec).name).isEqualTo("SearchMovieRelatedProjection")
        assertThat((codeGenResult.clientProjections[4].members[0] as TypeSpec).name).isEqualTo("SearchMovieRelatedVideoProjection")
        assertThat((codeGenResult.clientProjections[5].members[0] as TypeSpec).name).isEqualTo("SearchMovieRelatedVideoShowProjection")
        assertThat((codeGenResult.clientProjections[6].members[0] as TypeSpec).name).isEqualTo("SearchMovieRelatedVideoMovieProjection")
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

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, includeQueries = setOf("shows"), generateDataTypes = false, writeToFiles = false, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        assertThat(codeGenResult.dataTypes.size).isEqualTo(2)

        assertThat(codeGenResult.dataTypes).flatExtracting("members").extracting("name").containsExactly("ShowFilter", "SimilarityInput")
        assertThat(codeGenResult.enumTypes).flatExtracting("members").extracting("name").containsExactly("ShowType")
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
                language = Language.KOTLIN,
                generateClientApi = true,
                maxProjectionDepth = 0,
        )).generate() as KotlinCodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(2)
        val projectionTypeSpec = codeGenResult.clientProjections[0].members[0] as TypeSpec
        assertThat(projectionTypeSpec.name).isEqualTo("MoviesProjectionRoot")
        val actorTypeSpec = codeGenResult.clientProjections[1].members[0] as TypeSpec
        assertThat(actorTypeSpec.name).isEqualTo("MoviesActorsProjection")
    }
}