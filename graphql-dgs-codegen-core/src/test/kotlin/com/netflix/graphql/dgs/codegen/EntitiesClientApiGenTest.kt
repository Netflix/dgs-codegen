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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EntitiesClientApiGenTest {

    val basePackageName = "com.netflix.graphql.dgs.codegen.tests.generated"

    @Test
    fun generateForEntities() {
        val schema = """
            type Query {
                search: Movie
            }

            type Movie @key(fields: "movieId") {
                movieId: ID! @external
                title: String
                actor: Actor
            }

            type Actor {
                name: String
                friends: Actor
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        val projections = codeGenResult.clientProjections.filter {it.typeSpec.name.contains("Entities")}
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactly("onMovie")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieKeyActorProjection")
        assertThat(projections[3].typeSpec.name).isEqualTo("EntitiesMovieKeyActorFriendsProjection")

        val representations = codeGenResult.dataTypes.filter {it.typeSpec.name.contains("Representation")}
        assertThat(representations.size).isEqualTo(1)
        assertThat(representations[0].typeSpec.name).isEqualTo("MovieRepresentation")
        assertThat(representations[0].typeSpec.fieldSpecs).extracting("name").containsExactlyInAnyOrder("__typename", "movieId")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes))
    }

    @Test
    fun generateForEntitiesWithArraysAndNestedKeys() {
        val schema = """
            type Query {
                search: Movie
            }

            type Movie @key(fields: "movieId actors { name }") {
                movieId: ID! @external
                title: String
                actors: [Actor!]!
            }

            type Actor @key(fields: "name") {
                name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        val projections = codeGenResult.clientProjections.filter {it.typeSpec.name.contains("Entities")}
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactly("onMovie", "onActor")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieKeyActorsProjection")

        val representations = codeGenResult.dataTypes.filter {it.typeSpec.name.contains("Representation")}
        assertThat(representations.size).isEqualTo(2)
        assertThat(representations[0].typeSpec.name).isEqualTo("MovieRepresentation")
        assertThat(representations[0].typeSpec.fieldSpecs).extracting("name").containsExactlyInAnyOrder("__typename", "movieId", "actors")
        assertThat(representations[0].typeSpec.fieldSpecs[1]).extracting("type")
                .toString().contains("java.util.List<com.netflix.graphql.dgs.codegen.tests.generated.client.ActorRepresentation>")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes))
    }

    @Test
    fun generateForEntitiesWithNestedKeys() {
        val schema = """
            type Query {
                search: Movie
            }

            type Movie @key(fields: "movieId actor { name }") {
                movieId: ID! @external
                title: String
                actor: Person
            }

            type Person {
                name: String @external
                age: Int
            }
            
            type MovieCast @key(fields: "movie { movieId actor { name } } actor{name}") {
                movie: Movie
                actor: Person
            }

        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        val projections = codeGenResult.clientProjections.filter {it.typeSpec.name.contains("Entities")}
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactlyInAnyOrder("onMovie", "onMovieCast")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieKeyActorProjection")
        assertThat(projections[3].typeSpec.name).isEqualTo("EntitiesMovieCastKeyProjection")
        assertThat(projections[4].typeSpec.name).isEqualTo("EntitiesMovieCastKeyMovieProjection")
        assertThat(projections[5].typeSpec.name).isEqualTo("EntitiesMovieCastKeyMovieActorProjection")
        assertThat(projections[6].typeSpec.name).isEqualTo("EntitiesMovieCastKeyActorProjection")

        val representations = codeGenResult.dataTypes.filter {it.typeSpec.name.contains("Representation")}
        assertThat(representations.size).isEqualTo(3)
        assertThat(representations[0].typeSpec.name).isEqualTo("MovieRepresentation")
        assertThat(representations[0].typeSpec.fieldSpecs).extracting("name").containsExactlyInAnyOrder("__typename", "movieId", "actor")
        assertThat(representations[1].typeSpec.name).isEqualTo("PersonRepresentation")
        assertThat(representations[1].typeSpec.fieldSpecs).extracting("name").containsExactlyInAnyOrder("__typename", "name")
        assertThat(representations[2].typeSpec.name).isEqualTo("MovieCastRepresentation")
        assertThat(representations[2].typeSpec.fieldSpecs).extracting("name").containsExactlyInAnyOrder("__typename", "movie", "actor")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes))
    }

    @Test
    fun generateForEntitiesWithMultipleKeyEntities() {
        val schema = """
            type Query {
                search: Movie
            }

            type Movie @key(fields: "movieId") {
                movieId: ID! @external
                title: String
                actor: MovieActor
            }

            type MovieActor @key(fields: "name") {
                name: String @external
                age: Int
            }

        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        val projections = codeGenResult.clientProjections.filter {it.typeSpec.name.contains("Entities")}
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactlyInAnyOrder("onMovie", "onMovieActor")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieKeyActorProjection")
        assertThat(projections[3].typeSpec.name).isEqualTo("EntitiesMovieActorKeyProjection")

        val representations = codeGenResult.dataTypes.filter {it.typeSpec.name.contains("Representation")}
        assertThat(representations.size).isEqualTo(2)
        assertThat(representations[0].typeSpec.name).isEqualTo("MovieRepresentation")
        assertThat(representations[0].typeSpec.fieldSpecs).extracting("name").containsExactlyInAnyOrder("__typename", "movieId")
        assertThat(representations[1].typeSpec.name).isEqualTo("MovieActorRepresentation")
        assertThat(representations[1].typeSpec.fieldSpecs).extracting("name").containsExactlyInAnyOrder("__typename", "name")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes))
    }

    @Test
    fun generateForEntitiesWithNestedComplexKeys() {
        val schema = """
            type Query {
                search: Movie
            }

            type Movie @key(fields: "movieId actor { name age }") {
                movieId: ID! @external
                title: String
                actor: Person
            }

            type Person {
                name: String @external
                age: Int
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        val projections = codeGenResult.clientProjections.filter {it.typeSpec.name.contains("Entities")}
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactlyInAnyOrder("onMovie")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieKeyActorProjection")

        val representations = codeGenResult.dataTypes.filter {it.typeSpec.name.contains("Representation")}
        assertThat(representations.size).isEqualTo(2)
        assertThat(representations[0].typeSpec.name).isEqualTo("MovieRepresentation")
        assertThat(representations[0].typeSpec.fieldSpecs).extracting("name").containsExactlyInAnyOrder("__typename", "movieId", "actor")
        assertThat(representations[1].typeSpec.name).isEqualTo("PersonRepresentation")
        assertThat(representations[1].typeSpec.fieldSpecs).extracting("name").containsExactlyInAnyOrder("__typename", "name", "age")

        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes))
    }

    @Test
    fun testScalarsInEntities() {
        val schema = """
          type Query {
              movieCountry: MovieCountry
          }
          
          type MovieCountry @key(fields : "movieId country") {
            country: String
            movieId: Long
          }
          scalar Long

        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, typeMapping = mapOf(Pair("Long", "java.lang.Long")))).generate() as CodeGenResult
        val representations = codeGenResult.dataTypes.filter {it.typeSpec.name.contains("Representation")}
        assertThat(representations.size).isEqualTo(1)
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(3)
        assertCompiles(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes))
    }

    @Test
    fun skipEntities() {
        val schema = """
            type Query {
                search: Movie
            }

            type Movie @key(fields: "movieId") {
                movieId: ID! @external
                title: String
                actor: Actor
            }

            type Actor {
                name: String
                friends: Actor
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, skipEntityQueries = true)).generate() as CodeGenResult

        val projections = codeGenResult.clientProjections.filter {it.typeSpec.name.contains("Entities")}
        assertThat(projections).isEmpty()
    }
}