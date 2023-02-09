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
import org.assertj.core.api.Assertions.fail
import org.junit.Ignore
import org.junit.jupiter.api.Test

@Ignore
class EntitiesClientApiGenTest {

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

        val projections = codeGenResult.clientProjections
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").contains("onMovie")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("MovieGenreProjection")
        assertThat(projections[3].typeSpec.name).isEqualTo("ActorProjection")

        val representation = codeGenResult.javaDataTypes.single { "Representation" in it.typeSpec.name }
        assertThat(representation.typeSpec.name).isEqualTo("MovieRepresentation")
        assertThat(representation.typeSpec.fieldSpecs).extracting("name")
            .containsExactlyInAnyOrder("__typename", "movieId")

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

        val projections = codeGenResult.clientProjections
        assertThat(projections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(projections[1].typeSpec.name).isEqualTo("ActorProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[2].typeSpec.methodSpecs).extracting("name").contains("onMovie")
        assertThat(projections[3].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")

        val representation = codeGenResult.javaDataTypes.single { "Representation" in it.typeSpec.name }
        assertThat(representation.typeSpec.name).isEqualTo("MovieRepresentation")
        assertThat(representation.typeSpec.fieldSpecs).extracting("name")
            .containsExactlyInAnyOrder("__typename", "movieId")

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

        val projections = codeGenResult.clientProjections
        assertThat(projections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(projections[1].typeSpec.name).isEqualTo("IActorProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("ActorProjection")
        assertThat(projections[3].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[3].typeSpec.methodSpecs).extracting("name").contains("onMovie")
        assertThat(projections[4].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")

        val representations = codeGenResult.javaDataTypes.filter { "Representation" in it.typeSpec.name }
        assertThat(representations).hasSize(2)
        assertThat(representations[0].typeSpec.name).isEqualTo("MovieRepresentation")
        assertThat(representations[0].typeSpec.fieldSpecs).extracting("name")
            .containsExactlyInAnyOrder("__typename", "actor")
        assertThat(representations[1].typeSpec.name).isEqualTo("IActorRepresentation")
        assertThat(representations[1].typeSpec.fieldSpecs).extracting("name")
            .containsExactlyInAnyOrder("__typename", "name")

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

        val projections = codeGenResult.clientProjections
        assertThat(projections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(projections[1].typeSpec.name).isEqualTo("ActorProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[2].typeSpec.methodSpecs).extracting("name").contains("onMovie")
        assertThat(projections[3].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[4].typeSpec.name).isEqualTo("EntitiesActorKeyProjection")

        val representations = codeGenResult.javaDataTypes.filter { "Representation" in it.typeSpec.name }
        assertThat(representations).hasSize(2)
        assertThat(representations[0].typeSpec.name).isEqualTo("MovieRepresentation")
        assertThat(representations[0].typeSpec.fieldSpecs).extracting("name")
            .containsExactlyInAnyOrder("__typename", "movieId", "actors")
        assertThat(representations[0].typeSpec.fieldSpecs[1]).extracting("type")
            .toString()
            .contains("java.util.List<com.netflix.graphql.dgs.codegen.tests.generated.client.ActorRepresentation>")

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

        val projections = codeGenResult.clientProjections.filter { it.typeSpec.name.startsWith("Entities") }
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name")
            .containsExactlyInAnyOrder("<init>", "onMovie", "onMovieCast")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieCastKeyProjection")

        val representations = codeGenResult.javaDataTypes.filter { "Representation" in it.typeSpec.name }
        assertThat(representations).hasSize(3)
        assertThat(representations[0].typeSpec.name).isEqualTo("MovieRepresentation")
        assertThat(representations[0].typeSpec.fieldSpecs).extracting("name")
            .containsExactlyInAnyOrder("__typename", "movieId", "actor")
        assertThat(representations[1].typeSpec.name).isEqualTo("PersonRepresentation")
        assertThat(representations[1].typeSpec.fieldSpecs).extracting("name")
            .containsExactlyInAnyOrder("__typename", "name")
        assertThat(representations[2].typeSpec.name).isEqualTo("MovieCastRepresentation")
        assertThat(representations[2].typeSpec.fieldSpecs).extracting("name")
            .containsExactlyInAnyOrder("__typename", "movie", "actor")

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
            .containsExactlyInAnyOrder("<init>", "onMovie", "onMovieActor")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")
        assertThat(projections[2].typeSpec.name).isEqualTo("EntitiesMovieActorKeyProjection")

        val representations = codeGenResult.javaDataTypes.filter { "Representation" in it.typeSpec.name }
        assertThat(representations).hasSize(2)
        assertThat(representations[0].typeSpec.name).isEqualTo("MovieRepresentation")
        assertThat(representations[0].typeSpec.fieldSpecs).extracting("name")
            .containsExactlyInAnyOrder("__typename", "movieId")
        assertThat(representations[1].typeSpec.name).isEqualTo("MovieActorRepresentation")
        assertThat(representations[1].typeSpec.fieldSpecs).extracting("name")
            .containsExactlyInAnyOrder("__typename", "name")

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
        assertThat(projections[0].typeSpec.name).isEqualTo("EntitiesProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactlyInAnyOrder("<init>", "onMovie")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")

        val representations = codeGenResult.javaDataTypes.filter { "Representation" in it.typeSpec.name }
        assertThat(representations).hasSize(2)
        assertThat(representations[0].typeSpec.name).isEqualTo("MovieRepresentation")
        assertThat(representations[0].typeSpec.fieldSpecs).extracting("name")
            .containsExactlyInAnyOrder("__typename", "movieId", "actor")
        assertThat(representations[1].typeSpec.name).isEqualTo("PersonRepresentation")
        assertThat(representations[1].typeSpec.fieldSpecs).extracting("name")
            .containsExactlyInAnyOrder("__typename", "name", "age")

        codeGenResult.assertCompile()
    }

    @Test
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
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactlyInAnyOrder("<init>", "onMovie")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")

        val representations = codeGenResult.javaDataTypes.filter { "Representation" in it.typeSpec.name }
        assertThat(representations).hasSize(2)
        assertThat(representations[0].typeSpec.name).isEqualTo("MovieRepresentation")
        assertThat(representations[0].typeSpec.fieldSpecs).extracting("name")
            .containsExactlyInAnyOrder("__typename", "id", "genre")
        assertThat(representations[1].typeSpec.name).isEqualTo("MovieGenreRepresentation")

        codeGenResult.assertCompile()
    }

    @Test
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
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactlyInAnyOrder("<init>", "onMovie")
        assertThat(projections[1].typeSpec.name).isEqualTo("EntitiesMovieKeyProjection")

        val representations = codeGenResult.javaDataTypes.filter { "Representation" in it.typeSpec.name }
        assertThat(representations.map { it.typeSpec.name })
            .containsExactlyInAnyOrder(
                "MovieRepresentation",
                "MovieGenreRepresentation",
                "PersonRepresentation",
                "LocationRepresentation"
            )

        assertThat(representations.first { it.typeSpec.name == "MovieRepresentation" }.typeSpec.fieldSpecs)
            .extracting("name").containsExactlyInAnyOrder("__typename", "id", "actor", "genre", "location")

        val movieRepresentationType = representations.find { it.typeSpec.name == "MovieRepresentation" }
            ?: fail("MovieRepresentation type not found")
        assertThat(movieRepresentationType.typeSpec.fieldSpecs.map { it.name to it.type.toString() })
            .containsExactlyInAnyOrder(
                "id" to "java.lang.String",
                "genre" to "com.netflix.graphql.dgs.codegen.tests.generated.client.MovieGenreRepresentation",
                "actor" to "com.netflix.graphql.dgs.codegen.tests.generated.client.PersonRepresentation",
                "location" to "com.netflix.graphql.dgs.codegen.tests.generated.client.LocationRepresentation",
                "__typename" to "java.lang.String"
            )

        assertThat(representations.first { it.typeSpec.name == "PersonRepresentation" }.typeSpec.fieldSpecs)
            .extracting("name").containsExactlyInAnyOrder("__typename", "id")

        assertThat(representations.first { it.typeSpec.name == "LocationRepresentation" }.typeSpec.fieldSpecs)
            .extracting("name").containsExactlyInAnyOrder("__typename", "id")

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

        val representations = codeGenResult.javaDataTypes.filter { "Representation" in it.typeSpec.name }
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
                skipEntityQueries = true
            )
        ).generate()

        val projections = codeGenResult.clientProjections.filter { "Entities" in it.typeSpec.name }
        assertThat(projections).isEmpty()

        codeGenResult.assertCompile()
    }

    @Test
    fun `Generate projections for the entities' keys`() {
        val schema = """
            type Foo @key(fields:"id") {
              id: ID
              stringField: String
              barField: Bar
              mStringField(arg1: Int, arg2: String): [String!]
              mBarField(arg1: Int, arg2: String): [Bar!]
            }
            
            type Bar {
                id: ID
                baz: String
            }
        """.trimIndent()

        val codeGenResult = codeGen(schema)
        // then
        val testClassLoader = codeGenResult.assertCompile().toClassLoader()
        // assert projection classes
        val (entityRootProjectionClass, entitiesFooKeyProjectionClass, barProjectionClass) =
            arrayOf(
                "EntitiesProjectionRoot",
                "EntitiesFooKeyProjection",
                "BarProjection"
            ).map {
                val clazzCanonicalName = "$basePackageName.client.$it"
                val clazz = testClassLoader.loadClass(clazzCanonicalName)
                assertThat(clazz).describedAs(clazzCanonicalName).isNotNull
                clazz
            }

        // assert classes methods...
        assertThat(entityRootProjectionClass).isNotNull.hasPublicMethods("onFoo")

        assertThat(entitiesFooKeyProjectionClass).isNotNull.hasPublicMethods(
            "id",
            "stringField",
            "barField",
            "mStringField",
            "mBarField"
        )
        // entitiesFooKeyProjectionClass methods
        mapOf(
            "id" to entitiesFooKeyProjectionClass,
            "stringField" to entitiesFooKeyProjectionClass,
            "barField" to barProjectionClass,
            "mStringField" to entitiesFooKeyProjectionClass,
            "mBarField" to barProjectionClass
        ).forEach { (name, returnClass) ->
            assertThat(entitiesFooKeyProjectionClass.getMethod(name))
                .describedAs("${entitiesFooKeyProjectionClass.name} method: $name").isNotNull.returns(returnClass) { it.returnType }
        }

        mapOf(
            "mBarField" to (arrayOf(Integer::class.java, String::class.java) to barProjectionClass),
            "mStringField" to (arrayOf(Integer::class.java, String::class.java) to entitiesFooKeyProjectionClass)
        ).forEach { (name, p) ->
            val (_, returnClass) = p
            assertThat(entitiesFooKeyProjectionClass.getMethod(name))
                .describedAs("method: $name").isNotNull.returns(returnClass) { it.returnType }
        }
    }

    companion object {
        fun codeGen(schema: String): CodeGenResult {
            return CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    generateClientApi = true,
                    language = Language.JAVA
                )
            ).generate()
        }
        fun CodeGenResult.assertCompile() = assertCompilesJava(this)
    }
}
