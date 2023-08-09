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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClientApiGenMutationTest {
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApiv2 = true
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("UpdateMovieGraphQLQuery")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApiv2 = true
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("UpdateMovieGraphQLQuery")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaDataTypes
        )
    }

    @Test
    fun generateMutationWithInputDescription() {
        val schema = """
            type Mutation {
                updateMovie(
                ""${'"'}
                Some movie description
                ""${'"'}
                movie: MovieDescription): Movie
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApiv2 = true
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].javadoc.toString()).isEqualTo("Some movie description")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaDataTypes
        )
    }

    @Test
    fun generateMutationAddsNullChecksDuringInit() {
        val schema = """
            type Mutation {
                updateMovie(movie: MovieDescription, reviews: [String], uuid: UUID): Movie
            }
            
            input MovieDescription {
                movieId: Int
                title: String
                actors: [String]
            }
            
            type Movie {
                movieId: Int
                lastname: String
            }
            
            scalar UUID @javaType(name : "java.util.UUID")
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApiv2 = true
            )
        ).generate()

        val initMethod = codeGenResult.javaQueryTypes[0].typeSpec.methodSpecs
            .find { it.name == "<init>" }?.code.toString()

        val expected = """
            |super("mutation", queryName);
            |if (movie != null || fieldsSet.contains("movie")) {
            |    getInput().put("movie", movie);
            |}if (reviews != null || fieldsSet.contains("reviews")) {
            |    getInput().put("reviews", reviews);
            |}if (uuid != null || fieldsSet.contains("uuid")) {
            |    getInput().put("uuid", uuid);
            |}
        """.trimMargin()

        assert(initMethod.contains(expected))
        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaDataTypes
        )
    }

    @Test
    fun generateMutationDoesNotAddNullChecksForPrimitiveTypesDuringInit() {
        val schema = """
            type Mutation {
                updateMovie(movieId: Int!): Movie
            }
            
            type Movie {
                movieId: Int
                lastname: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApiv2 = true
            )
        ).generate()

        assert(
            codeGenResult.javaQueryTypes[0].typeSpec.methodSpecs
                .find { it.name == "<init>" }?.code.toString()
                .contains("super(\"mutation\", queryName);\ngetInput().put(\"movieId\", movieId);")
        )

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaDataTypes
        )
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
                tags(from: Int, to: Int, sourceType: SourceType): [ShowTag]
                isLive(countryFilter: CountryFilter): Boolean
            }
            
            enum ShouldNotInclude { YES, NO }
            
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
            
            input CountryFilter {
                countriesToExclude: [String]
            }
                 
            enum SourceType { FOO, BAR }
           
            type Person {
                name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApiv2 = true,
                includeMutations = setOf("shows"),
                generateDataTypes = false,
                writeToFiles = false
            )
        ).generate()

        assertThat(codeGenResult.javaDataTypes)
            .extracting("typeSpec").extracting("name").containsExactly("ShowFilter", "SimilarityInput", "CountryFilter")
        assertThat(codeGenResult.javaEnumTypes)
            .extracting("typeSpec").extracting("name").containsExactly("ShowType", "SourceType")
        assertThat(codeGenResult.javaQueryTypes)
            .extracting("typeSpec").extracting("name").containsExactly("ShowsGraphQLQuery")
        assertThat(codeGenResult.clientProjections)
            .extracting("typeSpec").extracting("name").containsExactly("ShowsProjectionRoot", "BooleanProjection")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaDataTypes + codeGenResult.javaEnumTypes)
    }

    @Test
    fun includeMutationConfig() {
        val schema = """
            type Mutation {
                updateMovieTitle: String
                addActorName: Boolean
            }           
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApiv2 = true,
                includeMutations = setOf("updateMovieTitle")
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("UpdateMovieTitleGraphQLQuery")

        assertCompilesJava(codeGenResult)
    }
}
