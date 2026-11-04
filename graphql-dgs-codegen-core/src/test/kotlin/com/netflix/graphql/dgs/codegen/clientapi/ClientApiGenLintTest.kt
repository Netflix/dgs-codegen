/*
 *
 *  Copyright 2026 Netflix, Inc.
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

import com.netflix.graphql.dgs.codegen.BASE_PACKAGE_NAME
import com.netflix.graphql.dgs.codegen.CodeGen
import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.assertCompilesJava
import com.palantir.javapoet.MethodSpec
import com.palantir.javapoet.TypeSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

/**
 * Generated client code should compile cleanly for projects that build with `-Xlint:all -Werror` on JDK 21+.
 * See https://github.com/Netflix/dgs-codegen/issues/651
 */
class ClientApiGenLintTest {
    private val schema =
        """
        type Query {
            movies(titleFilter: String, limit: Int! = 10, filter: MovieFilter): [Movie]
            show(id: ID!): Show
            search(text: String!): [SearchResult]
            everything: [Movie]
        }

        type Mutation {
            addMovie(input: MovieInput!): Movie
        }

        interface Show {
            id: ID!
            title: String
        }

        type Movie implements Show {
            id: ID!
            title: String
            actors(first: Int): [Actor]
        }

        type Series implements Show {
            id: ID!
            title: String
            seasons: Int
        }

        type Actor {
            name: String
        }

        type Review @key(fields: "id") {
            id: ID!
            stars: Int
        }

        union SearchResult = Movie | Actor

        input MovieFilter {
            genre: String
        }

        input MovieInput {
            title: String!
            filter: MovieFilter
        }
        """.trimIndent()

    private fun generate(): CodeGenResult =
        CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = BASE_PACKAGE_NAME,
                generateClientApi = true,
            ),
        ).generate()

    private fun CodeGenResult.type(name: String): TypeSpec =
        (javaQueryTypes + clientProjections).map { it.typeSpec() }.single { it.name() == name }

    private fun MethodSpec.suppressesThisEscape(): Boolean =
        annotations().any { it.toString() == "@java.lang.SuppressWarnings(\"this-escape\")" }

    @Test
    fun `generated client API compiles without this-escape warnings`() {
        assumeTrue(Runtime.version().feature() >= 21, "the this-escape lint needs javac 21+")

        assertCompilesJava(generate(), lintOptions = "-Xlint:all")
    }

    @Test
    fun `only constructors that touch base class state suppress this-escape`() {
        val codeGenResult = generate()

        val movies = codeGenResult.type("MoviesGraphQLQuery").methodSpecs().filter { it.isConstructor }
        assertThat(movies.filter { it.parameters().isNotEmpty() }).hasSize(2).allMatch { it.suppressesThisEscape() }
        assertThat(movies.single { it.parameters().isEmpty() }.suppressesThisEscape()).isFalse

        val everything = codeGenResult.type("EverythingGraphQLQuery").methodSpecs().filter { it.isConstructor }
        assertThat(everything).isNotEmpty.noneMatch { it.suppressesThisEscape() }

        for (fragment in listOf("MovieFragmentProjection", "SeriesFragmentProjection", "ActorFragmentProjection")) {
            val type = codeGenResult.type(fragment)
            assertThat(type.initializerBlock().isEmpty).isFalse
            assertThat(type.methodSpecs().filter { it.isConstructor }).isNotEmpty.allMatch { it.suppressesThisEscape() }
        }

        val entityKey = codeGenResult.type("EntitiesReviewKeyProjection")
        assertThat(entityKey.initializerBlock().isEmpty).isTrue
        assertThat(entityKey.methodSpecs().filter { it.isConstructor }).noneMatch { it.suppressesThisEscape() }

        val showProjection = codeGenResult.type("ShowProjectionRoot")
        assertThat(showProjection.methodSpecs().filter { it.isConstructor }).noneMatch { it.suppressesThisEscape() }
    }

    @Test
    fun `entities projection root creates key projections without raw types`() {
        val onReview = generate().type("EntitiesProjectionRoot").methodSpecs().single { it.name() == "onReview" }

        assertThat(onReview.code().toString()).contains("new EntitiesReviewKeyProjection<>(this, this)")
    }
}
