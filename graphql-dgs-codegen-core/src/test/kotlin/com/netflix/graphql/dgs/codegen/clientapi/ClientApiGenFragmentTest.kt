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

package com.netflix.graphql.dgs.codegen.clientapiv2

import com.netflix.graphql.dgs.codegen.*
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.ParameterizedTypeName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class ClientApiGenFragmentTest {
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs[0].name).isEqualTo("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Search_MovieProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("duration")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name")
            .doesNotContain("episodes")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("Search_SeriesProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("episodes")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name")
            .doesNotContain("duration")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaEnumTypes + codeGenResult.javaDataTypes + codeGenResult.javaInterfaces
        )
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(4)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Search_ShowProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("Search_Show_MovieProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("duration")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name")
            .doesNotContain("episodes")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("Search_Show_SeriesProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").contains("episodes")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name")
            .doesNotContain("duration")

        val superclass = codeGenResult.clientProjections[3].typeSpec.superclass as ParameterizedTypeName
        assertThat(superclass.typeArguments[1]).extracting("simpleName").isEqualTo("SearchProjectionRoot")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaEnumTypes + codeGenResult.javaDataTypes + codeGenResult.javaInterfaces
        )
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("onMovie")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("onActor")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Search_MovieProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("name")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("Search_ActorProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("name")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("title")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaEnumTypes + codeGenResult.javaDataTypes + codeGenResult.javaInterfaces
        )
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertThat(codeGenResult.clientProjections.size).isEqualTo(4)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Search_ResultProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("name")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("onMovie")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("onActor")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("Search_Result_MovieProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("name")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("Search_Result_ActorProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").contains("name")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").doesNotContain("title")

        assertThat(codeGenResult.clientProjections[2].typeSpec.initializerBlock.isEmpty).isFalse
        assertThat(codeGenResult.clientProjections[3].typeSpec.initializerBlock.isEmpty).isFalse

        val superclass = codeGenResult.clientProjections[3].typeSpec.superclass as ParameterizedTypeName
        assertThat(superclass.typeArguments[1]).extracting("simpleName").isEqualTo("SearchProjectionRoot")

        val searchResult = codeGenResult.javaInterfaces[0].typeSpec

        assertThat(JavaFile.builder("$basePackageName.types", searchResult).build().toString()).isEqualTo(
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
                |
            """.trimMargin()
        )

        // And assert the Search_Result_MovieProjection instance has an explicit schemaType
        val testClassLoader = assertCompilesJava(codeGenResult).toClassLoader()
        // Projection class
        val searchMovieProjectionClass =
            testClassLoader.loadClass("$basePackageName.client.Search_Result_MovieProjection")
        // Projection root and parent class
        val searchProjectionRootClass =
            testClassLoader.loadClass("$basePackageName.client.SearchProjectionRoot")
        val searchResultProjectionClass =
            testClassLoader.loadClass("$basePackageName.client.Search_ResultProjection")
        // Fetch constructor
        val searchMovieProjectionCtor =
            searchMovieProjectionClass.getDeclaredConstructor(searchResultProjectionClass, searchProjectionRootClass)
        val searchMovieProjectionInstance = searchMovieProjectionCtor.newInstance(null, null)

        val optionalProjectionSchemaType =
            invokeMethod<Optional<String>>(
                searchMovieProjectionClass.getMethod("getSchemaType"),
                searchMovieProjectionInstance
            )
        // assert we have the correct explicit type.
        assertThat(optionalProjectionSchemaType).contains("Movie")
    }
}
