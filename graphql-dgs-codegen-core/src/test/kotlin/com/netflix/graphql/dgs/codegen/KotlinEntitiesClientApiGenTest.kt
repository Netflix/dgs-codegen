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

import org.assertj.core.api.Assertions.*
import org.assertj.core.data.Index
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class KotlinEntitiesClientApiGenTest {

    @Test
    fun `We can have federated entities`() {
        val schema = """
            type Movie @key(fields: "movieId") {
                movieId: ID! @external
                title: String
                genre: MovieGenre
                actor: Actor
            }
            
            enum MovieGenre {
                HORROR
                ACTION
                ROMANCE
                COMEDY
            }

            type Actor {
                name: String
                friends: Actor
            }
        """.trimIndent()

        val codeGenResult = codeGen(schema)

        val projections = codeGenResult.clientProjections.filter { "Entities" in it.typeSpec.name }
        assertThat(projections)
            .hasSize(5)
            .satisfies(
                { file ->
                    assertThat(file.typeSpec.name).isEqualTo("EntitiesProjectionRoot")
                    assertThat(file.typeSpec.methodSpecs).extracting("name").containsExactly("onMovie")
                },
                Index.atIndex(0)
            )
            .satisfies(
                { file -> assertThat(file.typeSpec.name).isEqualTo("EntitiesMovieKeyProjection") },
                Index.atIndex(1)
            )
            .satisfies(
                { file -> assertThat(file.typeSpec.name).isEqualTo("EntitiesMovieKey_GenreProjection") },
                Index.atIndex(2)
            )
            .satisfies(
                { file -> assertThat(file.typeSpec.name).isEqualTo("EntitiesMovieKey_ActorProjection") },
                Index.atIndex(3)
            )
            .satisfies(
                { file -> assertThat(file.typeSpec.name).isEqualTo("EntitiesMovieKey_Actor_FriendsProjection") },
                Index.atIndex(4)
            )

        val representation = codeGenResult.kotlinDataTypes.single { "Representation" in it.name }

        assertThat(representation.name).isEqualTo("MovieRepresentation")
        codeGenResult.assertCompile()
    }

    @Test
    fun `We can have federated entities and queries`() {
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

        val codeGenResult = codeGen(schema)

        val projections = codeGenResult.clientProjections.filter { "Entities" in it.typeSpec.name }
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactly("onMovie")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieKey_ActorProjection")
        assertThat(projections[3].typeSpec.name).isEqualTo("EntitiesMovieKey_Actor_FriendsProjection")

        val representation = codeGenResult.kotlinDataTypes.single { "Representation" in it.name }
        assertThat(representation.name).isEqualTo("MovieRepresentation")
        codeGenResult.assertCompile()
    }

    @Test
    fun `An entity can have a field that is an interface`() {
        val schema = """
            type Query {
                search: Movie
            }

            type Movie @key(fields: "actor { name }") {
                movieId: ID!
                title: String
                actor: IActor @external
            }

            interface IActor {
                name: String 
            }
            
            type Actor implements IActor {
                name: String
                friends: Actor
            }
        """.trimIndent()

        val codeGenResult = codeGen(schema)

        val projections = codeGenResult.clientProjections.filter { "Entities" in it.typeSpec.name }
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactly("onMovie")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieKey_ActorProjection")
        assertThat(projections[3].typeSpec.name).isEqualTo("EntitiesMovieKey_Actor_ActorProjection")

        val representations = codeGenResult.kotlinDataTypes.filter { "Representation" in it.name }

        assertThat(representations).hasSize(2)
        assertThat(representations[0].name).isEqualTo("MovieRepresentation")
        assertThat(representations[1].name).isEqualTo("IActorRepresentation")

        codeGenResult.assertCompile()
    }

    @Test
    fun `We can have entities with arrays and nested keys`() {
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

        val codeGenResult = codeGen(schema)

        val projections = codeGenResult.clientProjections.filter { "Entities" in it.typeSpec.name }
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactly("onMovie", "onActor")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieKey_ActorsProjection")

        val representations = codeGenResult.kotlinDataTypes.filter { "Representation" in it.name }
        assertThat(representations).hasSize(2)
        assertThat(representations[0].name).isEqualTo("MovieRepresentation")

        codeGenResult.assertCompile()
    }

    @Test
    fun `We can have entities with nested keys`() {
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

        val codeGenResult = codeGen(schema)

        val projections = codeGenResult.clientProjections.filter { "Entities" in it.typeSpec.name }
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name")
            .containsExactlyInAnyOrder("onMovie", "onMovieCast")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieKey_ActorProjection")
        assertThat(projections[3].typeSpec.name).isEqualTo("EntitiesMovieCastKeyProjection")
        assertThat(projections[4].typeSpec.name).isEqualTo("EntitiesMovieCastKey_MovieProjection")
        assertThat(projections[5].typeSpec.name).isEqualTo("EntitiesMovieCastKey_Movie_ActorProjection")
        assertThat(projections[6].typeSpec.name).isEqualTo("EntitiesMovieCastKey_ActorProjection")

        val representations = codeGenResult.kotlinDataTypes.filter { "Representation" in it.name }
        assertThat(representations).hasSize(3)
        assertThat(representations[0].name).isEqualTo("MovieRepresentation")
        assertThat(representations[1].name).isEqualTo("PersonRepresentation")
        assertThat(representations[2].name).isEqualTo("MovieCastRepresentation")

        codeGenResult.assertCompile()
    }

    @Test
    fun `We can have multiple entities with keys`() {
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

        val codeGenResult = codeGen(schema)

        val projections = codeGenResult.clientProjections.filter { "Entities" in it.typeSpec.name }
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name")
            .containsExactlyInAnyOrder("onMovie", "onMovieActor")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieKey_ActorProjection")
        assertThat(projections[3].typeSpec.name).isEqualTo("EntitiesMovieActorKeyProjection")

        val representations = codeGenResult.kotlinDataTypes.filter { "Representation" in it.name }
        assertThat(representations).hasSize(2)
        assertThat(representations[0].name).isEqualTo("MovieRepresentation")
        assertThat(representations[1].name).isEqualTo("MovieActorRepresentation")

        codeGenResult.assertCompile()
    }

    @Test
    fun `Entities can have nested complex keys`() {
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

        val codeGenResult = codeGen(schema)

        val projections = codeGenResult.clientProjections.filter { "Entities" in it.typeSpec.name }
        assertThat(projections)
            .hasSize(3)
            .satisfies(
                { file ->
                    assertThat(file.typeSpec.name).isEqualTo("EntitiesProjectionRoot")
                    assertThat(file.typeSpec.methodSpecs).extracting("name").containsExactly("onMovie")
                },
                Index.atIndex(0)
            )
            .satisfies(
                { file -> assertThat(file.typeSpec.name).isEqualTo("EntitiesMovieKeyProjection") },
                Index.atIndex(1)
            )
            .satisfies(
                { file -> assertThat(file.typeSpec.name).isEqualTo("EntitiesMovieKey_ActorProjection") },
                Index.atIndex(2)
            )

        val representations = codeGenResult.kotlinDataTypes.filter { "Representation" in it.name }
        assertThat(representations).hasSize(2)
        assertThat(representations[0].name).isEqualTo("MovieRepresentation")
        assertThat(representations[1].name).isEqualTo("PersonRepresentation")

        codeGenResult.assertCompile()
    }

    // TODO MovieGenreRepresentation is missing.
    @Test
    @Disabled
    fun `Entities can have keys that are enums`() {
        val schema = """
            type Query {
                search: Movie
            }

            type Movie @key(fields: "id genre") {
                id: ID! @external
                genre: MovieGenre
                title: String
            }

            enum MovieGenre {
                HORROR
                ACTION
                ROMANCE
                COMEDY
            }
        """.trimIndent()

        val codeGenResult = codeGen(schema)

        val projections = codeGenResult.clientProjections.filter { "Entities" in it.typeSpec.name }
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactlyInAnyOrder("onMovie")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieKey_GenreProjection")

        val representations = codeGenResult.kotlinDataTypes.filter { "Representation" in it.name }
        assertThat(representations).hasSize(2)
        assertThat(representations[0].name).isEqualTo("MovieRepresentation")
        assertThat(representations[1].name).isEqualTo("MovieGenreRepresentation")

        codeGenResult.assertCompile()
    }

    // TODO understand why we are missing Person and MovieGenre Representations
    @Test
    @Disabled
    fun `Entities can have the @key directive used multiple times`() {
        val schema = """
            type Movie @key(fields: "id genre") @key(fields: "id actor{ id }") @key(fields: "id location { id }") {
                id: ID! @external
                title: String
                genre: MovieGenre
                actor: Person
                location: Location
            }
            
            enum MovieGenre {
                HORROR
                ACTION
                ROMANCE
                COMEDY
            }

            type Person @extends {
                id: ID @external
                name: String
                role: Role
            }
            
            enum Role { ATL BTL }
            
            type Location {
                id: ID
                name: String 
            }
        """.trimIndent()

        val codeGenResult = codeGen(schema)

        val projections = codeGenResult.clientProjections.filter { "Entities" in it.typeSpec.name }
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactlyInAnyOrder("onMovie")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieKey_GenreProjection")
        assertThat(projections[3].typeSpec.name).isEqualTo("EntitiesMovieKey_ActorProjection")
        assertThat(projections[4].typeSpec.name).isEqualTo("EntitiesMovieKey_Actor_RoleProjection")
        assertThat(projections[5].typeSpec.name).isEqualTo("EntitiesMovieKey_LocationProjection")

        val representations = codeGenResult.kotlinDataTypes.filter { "Representation" in it.name }
        assertThat(representations.map { it.name })
            .containsExactlyInAnyOrder(
                "MovieRepresentation",
                "MovieGenreRepresentation",
                "PersonRepresentation",
                "LocationRepresentation"
            )
        codeGenResult.assertCompile()
    }

    @Test
    fun `Entities can have scalar fields`() {
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

        val codeGenResult = codeGen(schema)

        val representations = codeGenResult.kotlinDataTypes.filter { "Representation" in it.name }
        assertThat(representations).hasSize(1)
        val projections = codeGenResult.clientProjections
        assertThat(projections).hasSize(3)

        codeGenResult.assertCompile()
    }

    @Test
    fun `CodeGen can be configured to skip entities`() {
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                skipEntityQueries = true,
                language = Language.KOTLIN
            )
        ).generate()

        val projections = codeGenResult.clientProjections.filter { "Entities" in it.typeSpec.name }
        assertThat(projections).isEmpty()

        codeGenResult.assertCompile()
    }

    companion object {
        fun codeGen(schema: String): CodeGenResult {
            return CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    generateClientApi = true,
                    language = Language.KOTLIN
                )
            ).generate()
        }

        fun CodeGenResult.assertCompile() = assertCompilesKotlin(this)
    }
}
