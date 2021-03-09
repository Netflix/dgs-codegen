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

import com.squareup.kotlinpoet.TypeSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KotlinEntitiesClientApiGenTest {

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

        val codeGenResult = CodeGen(CodeGenConfig(
            schemas = setOf(schema),
            packageName = basePackageName,
            language = Language.KOTLIN,
            generateClientApi = true,
        )).generate() as KotlinCodeGenResult

        val projections = codeGenResult.clientProjections.filter { it.name.contains("Entities")}
        assertThat(projections[0].name).isEqualTo("EntitiesProjectionRoot")
        assertThat((projections[0].members[0] as TypeSpec).funSpecs).extracting("name").containsExactly("onMovie")
        assertThat(projections[1].name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].name).isEqualTo("EntitiesMovieKeyActorProjection")
        assertThat(projections[3].name).isEqualTo("EntitiesMovieKeyActorFriendsProjection")

        val representations = codeGenResult.dataTypes.filter {it.name.contains("Representation")}
        assertThat(representations.size).isEqualTo(1)
        assertThat(representations[0].name).isEqualTo("MovieRepresentation")
        assertThat((representations[0].members[0] as TypeSpec).propertySpecs).extracting("name").containsExactlyInAnyOrder("__typename", "movieId")
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

        val codeGenResult = CodeGen(CodeGenConfig(
            schemas = setOf(schema),
            packageName = basePackageName,
            language = Language.KOTLIN,
            generateClientApi = true,
        )).generate() as KotlinCodeGenResult

        val projections = codeGenResult.clientProjections.filter {it.name.contains("Entities")}
        assertThat(projections[0].name).isEqualTo("EntitiesProjectionRoot")
        assertThat((projections[0].members[0] as TypeSpec).funSpecs).extracting("name").containsExactly("onMovie", "onActor")
        assertThat(projections[1].name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].name).isEqualTo("EntitiesMovieKeyActorsProjection")

        val representations = codeGenResult.dataTypes.filter {it.name.contains("Representation")}
        assertThat(representations.size).isEqualTo(2)
        assertThat(representations[0].name).isEqualTo("MovieRepresentation")
        assertThat((representations[0].members[0] as TypeSpec).propertySpecs).extracting("name").containsExactlyInAnyOrder("__typename", "movieId", "actors")
        assertThat((representations[0].members[0] as TypeSpec).propertySpecs[1]).extracting("type")
                .toString().contains("List<com.netflix.graphql.dgs.codegen.tests.generated.client.ActorRepresentation>")
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

            type Person @key(fields: "name") {
                name: String @external
                age: Int
            }
            
            type MovieCast @key(fields: "movie { movieId actor { name } } actor{name}") {
                movie: Movie
                actor: Person
            }

        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(
            schemas = setOf(schema),
            packageName = basePackageName,
            language = Language.KOTLIN,
            generateClientApi = true,
        )).generate() as KotlinCodeGenResult

        val projections = codeGenResult.clientProjections.filter {it.name.contains("Entities")}
        assertThat(projections[0].name).isEqualTo("EntitiesProjectionRoot")
        assertThat((projections[0].members[0] as TypeSpec).funSpecs).extracting("name").containsExactlyInAnyOrder("onMovie", "onPerson", "onMovieCast")
        assertThat(projections[1].name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].name).isEqualTo("EntitiesMovieKeyActorProjection")
        assertThat(projections[3].name).isEqualTo("EntitiesPersonKeyProjection")
        assertThat(projections[4].name).isEqualTo("EntitiesMovieCastKeyProjection")
        assertThat(projections[5].name).isEqualTo("EntitiesMovieCastKeyMovieProjection")
        assertThat(projections[6].name).isEqualTo("EntitiesMovieCastKeyMovieActorProjection")
        assertThat(projections[7].name).isEqualTo("EntitiesMovieCastKeyActorProjection")

        val representations = codeGenResult.dataTypes.filter {it.name.contains("Representation")}
        assertThat(representations.size).isEqualTo(3)
        assertThat(representations[0].name).isEqualTo("MovieRepresentation")
        assertThat((representations[0].members[0] as TypeSpec).propertySpecs).extracting("name").containsExactlyInAnyOrder("__typename", "movieId", "actor")
        assertThat(representations[1].name).isEqualTo("PersonRepresentation")
        assertThat((representations[1].members[0] as TypeSpec).propertySpecs).extracting("name").containsExactlyInAnyOrder("__typename", "name")
        assertThat(representations[2].name).isEqualTo("MovieCastRepresentation")
        assertThat((representations[2].members[0] as TypeSpec).propertySpecs).extracting("name").containsExactlyInAnyOrder("__typename", "movie", "actor")
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

        val codeGenResult = CodeGen(CodeGenConfig(
            schemas = setOf(schema),
            packageName = basePackageName,
            language = Language.KOTLIN,
            generateClientApi = true,
        )).generate() as KotlinCodeGenResult

        val projections = codeGenResult.clientProjections.filter {it.name.contains("Entities")}
        assertThat(projections[0].name).isEqualTo("EntitiesProjectionRoot")
        assertThat((projections[0].members[0] as TypeSpec).funSpecs).extracting("name").containsExactlyInAnyOrder("onMovie", "onMovieActor")
        assertThat(projections[1].name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].name).isEqualTo("EntitiesMovieKeyActorProjection")
        assertThat(projections[3].name).isEqualTo("EntitiesMovieActorKeyProjection")

        val representations = codeGenResult.dataTypes.filter {it.name.contains("Representation")}
        assertThat(representations.size).isEqualTo(2)
        assertThat(representations[0].name).isEqualTo("MovieRepresentation")
        assertThat((representations[0].members[0] as TypeSpec).propertySpecs).extracting("name").containsExactlyInAnyOrder("__typename", "movieId")
        assertThat(representations[1].name).isEqualTo("MovieActorRepresentation")
        assertThat((representations[1].members[0] as TypeSpec).propertySpecs).extracting("name").containsExactlyInAnyOrder("__typename", "name")
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

        val codeGenResult = CodeGen(CodeGenConfig(
            schemas = setOf(schema),
            packageName = basePackageName,
            language = Language.KOTLIN,
            generateClientApi = true,
        )).generate() as KotlinCodeGenResult

        val projections = codeGenResult.clientProjections.filter {it.name.contains("Entities")}
        assertThat(projections[0].name).isEqualTo("EntitiesProjectionRoot")
        assertThat((projections[0].members[0] as TypeSpec).funSpecs).extracting("name").containsExactlyInAnyOrder("onMovie")
        assertThat(projections[1].name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].name).isEqualTo("EntitiesMovieKeyActorProjection")

        val representations = codeGenResult.dataTypes.filter {it.name.contains("Representation")}
        assertThat(representations.size).isEqualTo(2)
        assertThat(representations[0].name).isEqualTo("MovieRepresentation")
        assertThat((representations[0].members[0] as TypeSpec).propertySpecs).extracting("name").containsExactlyInAnyOrder("__typename", "movieId", "actor")
        assertThat(representations[1].name).isEqualTo("PersonRepresentation")
        assertThat((representations[1].members[0] as TypeSpec).propertySpecs).extracting("name").containsExactlyInAnyOrder("__typename", "name", "age")
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

        val codeGenResult = CodeGen(CodeGenConfig(
            schemas = setOf(schema),
            packageName = basePackageName,
            language = Language.KOTLIN,
            generateClientApi = true,
            typeMapping = mapOf(Pair("Long", "java.lang.Long")),
        )).generate() as KotlinCodeGenResult
        val representations = codeGenResult.dataTypes.filter {it.name.contains("Representation")}
        assertThat(representations.size).isEqualTo(1)
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(3)
    }
}