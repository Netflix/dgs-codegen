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

class ClientApiGenProjectionForDefinedQueriesTest {
    @Test
    fun `Generated root projection should contain only selected fields from the query`() {

        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
            
            query {
                people {
                    firstname
                }
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                generateClientApiForDefinedQuery = true,
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(1)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PeopleProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("firstname")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").doesNotContain("lastname")

        assertCompilesJava(codeGenResult.clientProjections)
    }

    @Test
    fun `Projections should be generated only for selected fields for cycles`() {

        val schema = """
            type Query @extends {
                persons: [Person]
            }

            type Person {
             name: String
             friends: [Person]
            }
            
            query {
                persons {
                    name
                }
            }    
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                generateClientApiForDefinedQuery = true,
            )
        ).generate()
        assertThat(codeGenResult.clientProjections.size).isEqualTo(1)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PersonsProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("name")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").doesNotContain("friends")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
    }

    @Test
    fun `Projections for interfaces should be generated only for selected fields`() {
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
            
            type Series implements Show {
                title: String
            }
            
            query {
                search {
                    title
                    ... On Movie {
                        details {
                            show {
                               title
                               ... On Movie {
                                   duration
                               }
                               ... On Series {
                                   title
                               }
                            }
                        }
                    }
                }
            
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                generateClientApiForDefinedQuery = true,
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(6)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Search_MovieProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("Search_Movie_DetailsProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("Search_Movie_Details_ShowProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").contains("onMovie")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").contains("onSeries")
        assertThat(codeGenResult.clientProjections[4].typeSpec.name).isEqualTo("Search_Movie_Details_Show_MovieProjection")
        assertThat(codeGenResult.clientProjections[4].typeSpec.methodSpecs).extracting("name").contains("duration")
        assertThat(codeGenResult.clientProjections[4].typeSpec.methodSpecs).extracting("name").doesNotContain("details")
        assertThat(codeGenResult.clientProjections[4].typeSpec.methodSpecs).extracting("name").doesNotContain("title")
        assertThat(codeGenResult.clientProjections[5].typeSpec.name).isEqualTo("Search_Movie_Details_Show_SeriesProjection")
        assertThat(codeGenResult.clientProjections[5].typeSpec.methodSpecs).extracting("name").contains("title")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaEnumTypes + codeGenResult.javaDataTypes + codeGenResult.javaInterfaces
        )
    }

    @Test
    fun `Projections for unions should only be generated for selected fields`() {
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
            
            query {
              search(title: "sdsd") {
                ... On Show {
                  title
                }
                ... On Movie {
                  related {
                    video {
                        ... On Movie {
                            duration
                        }
                    }
                  }
                }
              }
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                generateClientApiForDefinedQuery = true,
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(6)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("onMovie")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("onShow")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Search_ShowProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("Search_MovieProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("Search_Movie_RelatedProjection")
        assertThat(codeGenResult.clientProjections[4].typeSpec.name).isEqualTo("Search_Movie_Related_VideoProjection")
        assertThat(codeGenResult.clientProjections[4].typeSpec.methodSpecs).extracting("name").contains("onMovie")
        assertThat(codeGenResult.clientProjections[4].typeSpec.methodSpecs).extracting("name").doesNotContain("onShow")
        assertThat(codeGenResult.clientProjections[5].typeSpec.name).isEqualTo("Search_Movie_Related_Video_MovieProjection")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaEnumTypes + codeGenResult.javaDataTypes + codeGenResult.javaInterfaces
        )
    }

    @Test
    fun `Subprojections should only be generated for selected fields `() {

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
            
            query {
                movies {
                    actors {
                        age
                    }
                }
            }
           
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                generateClientApiForDefinedQuery = true,
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(2)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("MoviesProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").doesNotContain("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Movies_ActorsProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("name")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
    }

    @Test
    fun `Projections are generated for selected fields when schema has extended type definitions`() {
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

          query {
              people {
                email
              }
          }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                generateClientApiForDefinedQuery = true,
                typeMapping = mapOf("Long" to "java.lang.Long"),
            )
        ).generate()
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(1)
        assertThat(projections[0].typeSpec.name).isEqualTo("PeopleProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs.size).isEqualTo(1)
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactly("email")

        assertCompilesJava(codeGenResult)
    }
}
