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

class ClientApiGenSubscriptionTest {
    @Test
    fun generateSubscriptionType() {
        val schema = """
            type Subscription {
                movie(movieId: ID, title: String): Movie
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
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("MovieGraphQLSubscription")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
    }

    @Test
    fun generateSubscriptionWithInputType() {
        val schema = """
            type Mutation {
                movie(movie: MovieDescription): Movie
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
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("MovieGraphQLMutation")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaDataTypes
        )
    }

    @Test
    fun includeSubscriptionConfig() {
        val schema = """
            type Subscription {
                movieTitle: String
                addActorName: Boolean
            }           
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApiv2 = true,
                includeSubscriptions = setOf("movieTitle")
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("MovieTitleGraphQLSubscription")

        assertCompilesJava(codeGenResult)
    }
}
