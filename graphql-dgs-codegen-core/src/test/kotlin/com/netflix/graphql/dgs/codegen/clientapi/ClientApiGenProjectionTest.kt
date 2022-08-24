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

package com.netflix.graphql.dgs.codegen.clientapi

import com.netflix.graphql.dgs.codegen.CodeGen
import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.assertCompilesJava
import com.netflix.graphql.dgs.codegen.basePackageName
import com.squareup.javapoet.ClassName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class ClientApiGenProjectionTest {
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(1)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PeopleProjectionRoot")

        assertCompilesJava(codeGenResult.clientProjections)
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()
        assertThat(codeGenResult.clientProjections.size).isEqualTo(2)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PersonsProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Persons_FriendsProjection")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("name")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("friends")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("name")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(5)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Search_MovieProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("Search_Movie_DetailsProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("Search_Movie_Details_ShowProjection")
        assertThat(codeGenResult.clientProjections[4].typeSpec.name).isEqualTo("Search_Movie_Details_Show_MovieProjection")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaEnumTypes + codeGenResult.javaDataTypes + codeGenResult.javaInterfaces
        )
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(7)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Search_ShowProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("Search_MovieProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("Search_Movie_RelatedProjection")
        assertThat(codeGenResult.clientProjections[4].typeSpec.name).isEqualTo("Search_Movie_Related_VideoProjection")
        assertThat(codeGenResult.clientProjections[5].typeSpec.name).isEqualTo("Search_Movie_Related_Video_ShowProjection")
        assertThat(codeGenResult.clientProjections[6].typeSpec.name).isEqualTo("Search_Movie_Related_Video_MovieProjection")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaEnumTypes + codeGenResult.javaDataTypes + codeGenResult.javaInterfaces
        )
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PersonsProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("FriendsProjectionRoot")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PersonsProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Persons_DetailsProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("DetailsProjectionRoot")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
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
                type: MovieType
            }
            
            type Actor {
                name: String
                age: Integer
            }
           
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(2)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("MoviesProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Movies_ActorsProjection")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
    }

    @Test
    fun generateSubProjectionTypesWithSimilarQueryAndFieldNames() {
        val schema = """
            type Query {
                user: User
            }

            type User {
                favoriteMovie: Movie
                favoriteMovieGenre: Genre
            }

            type Movie {
                genre: Genre
            }

            type Genre {
                name: String
            }      
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(4)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("UserProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("User_FavoriteMovieProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("User_FavoriteMovie_GenreProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("User_FavoriteMovieGenreProjection")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                shortProjectionNames = true
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("MoviesProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Movies_ActorsProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("Mo_Ac_MoviesProjection")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                typeMapping = mapOf("Long" to "java.lang.Long")
            )
        ).generate()
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(1)
        assertThat(projections[0].typeSpec.name).isEqualTo("PeopleProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs.size).isEqualTo(2)
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactly("name", "email")

        assertCompilesJava(codeGenResult)
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                typeMapping = mapOf("Long" to "java.lang.Long")
            )
        ).generate()
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(2)
        assertThat(projections[1].typeSpec.name).isEqualTo("Search_MovieProjection")
        assertThat(projections[1].typeSpec.methodSpecs.size).isEqualTo(3)
        assertThat(projections[1].typeSpec.methodSpecs).extracting("name").contains("title", "director", "<init>")

        assertCompilesJava(codeGenResult)
    }

    @Test
    fun testExtendSubProjectionOutOfOrder() {
        val schema = """
          type Query {
            search: [SearchResult]
          }

          type SearchResult {
            movie: Movie
          }

          extend type Movie {
            director: String
          }

          type Movie {
            title: String
          }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                typeMapping = mapOf("Long" to "java.lang.Long")
            )
        ).generate()
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(2)
        assertThat(projections[1].typeSpec.name).isEqualTo("Search_MovieProjection")
        assertThat(projections[1].typeSpec.methodSpecs.size).isEqualTo(3)
        assertThat(projections[1].typeSpec.methodSpecs).extracting("name").contains("title", "director", "<init>")

        assertCompilesJava(codeGenResult)
    }

    @Test
    fun generateSubProjectionTypesMaxDepth() {
        val schema = """
            type Query {
                movies: [Movie]
            }
            
            type Movie {
                title: String
                rating: Rating
                actors: [Actor]
            }
            
            type Actor {
                name: String
                age: Integer
                agent: Agent
            }
            
            type Agent {
                name: String  
                address : Address
            }
            
            type Address {
                street: String
            }
            
            type Rating {
                starts: Integer
                review: Review 
            }
            
            type Review {
                description: String
            }
                

        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                maxProjectionDepth = 2
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(5)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("MoviesProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Movies_RatingProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("Movies_Rating_ReviewProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("Movies_ActorsProjection")
        assertThat(codeGenResult.clientProjections[4].typeSpec.name).isEqualTo("Movies_Actors_AgentProjection")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
    }

    @Test
    fun testImplementsInterfaceProjection() {
        val schema = """
            type Query {
                search(title: String): [Show]
            }
            
            interface Show {
                title: String
                director: Director
            }
            
            interface Person {
                name: String
            }
            
            type Director implements Person {
                name: String
                shows: [Show]
            }
            
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("SearchGraphQLQuery")
        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("director")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Search_DirectorProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("shows")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("name")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaEnumTypes + codeGenResult.javaDataTypes + codeGenResult.javaInterfaces
        )
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                typeMapping = mapOf("Long" to "java.lang.Long")
            )
        ).generate()
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(1)
        assertCompilesJava(codeGenResult)
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
                short: Integer
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(1)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("WeirdTypeProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name")
            .containsExactly("__", "_root", "_parent", "_import", "_short")

        assertCompilesJava(codeGenResult)
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
                short: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(2)
        val weirdType = codeGenResult.clientProjections.find { it.typeSpec.name == "NormalType_WeirdTypeProjection" }
            ?: fail("NormalType_WeirdTypeProjection type not found")

        assertThat(weirdType.typeSpec.methodSpecs).extracting("name")
            .contains("__", "_root", "_parent", "_import", "_short")

        assertCompilesJava(codeGenResult.clientProjections)
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                writeToFiles = false
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(6)
        val workshopAssetsReviewsProjection =
            codeGenResult.clientProjections.find { it.typeSpec.name == "Workshop_Assets_ReviewsProjection" }
                ?: fail("Workshop_Assets_ReviewsProjection type not found")
        val workshopReviewsProjection =
            codeGenResult.clientProjections.find { it.typeSpec.name == "Workshop_ReviewsProjection" }
                ?: fail("Workshop_ReviewsProjection type not found")

        assertThat(workshopReviewsProjection.typeSpec.methodSpecs).extracting("name").contains("edges")
        assertThat(workshopAssetsReviewsProjection.typeSpec.methodSpecs).extracting("name").contains("edges")
    }

    @Test
    fun `Input arguments on root projections should be support in the query API`() {
        val schema = """
            type Query {
                movies: [Movie]
            }
            
            type Movie {
                actors(leadCharactersOnly: Boolean): [Actor]
            }
            
            type Actor {
                name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                maxProjectionDepth = 2
            )
        ).generate()

        val methodSpecs = codeGenResult.clientProjections[0].typeSpec.methodSpecs
        assertThat(methodSpecs.size).isEqualTo(2)
        val methodWithArgs = methodSpecs.find { it.parameters.size > 0 }
            ?: fail("Expected method not found")
        assertThat(methodWithArgs.parameters[0].name).isEqualTo("leadCharactersOnly")
        assertThat(methodWithArgs.parameters[0].type.toString()).isEqualTo("java.lang.Boolean")
    }

    @Test
    fun `Input arguments on sub projections should be support in the query API`() {
        val schema = """
            type Query {
                movies: [Movie]
            }

            type Movie {
                actors: [Actor]
                awards(oscarsOnly: Boolean): [Award!]
            }

            type Actor {
                awards(oscarsOnly: Boolean): [Award!]
            }

            type Award {
                name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                writeToFiles = true
            )
        ).generate()

        val methodSpecs = codeGenResult.clientProjections[1].typeSpec.methodSpecs
        val methodWithArgs = methodSpecs.find { !it.isConstructor && it.parameters.size > 0 }
            ?: fail("Method not found")
        assertThat(methodWithArgs.returnType).extracting { (it as ClassName).simpleName() }
            .isEqualTo("Movies_Actors_AwardsProjection")
        assertThat(methodWithArgs.parameters[0].name).isEqualTo("oscarsOnly")
        assertThat(methodWithArgs.parameters[0].type.toString()).isEqualTo("java.lang.Boolean")
    }
}
