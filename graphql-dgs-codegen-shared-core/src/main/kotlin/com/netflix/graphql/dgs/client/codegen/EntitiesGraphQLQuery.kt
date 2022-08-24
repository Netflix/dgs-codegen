/*
 * Copyright 2020 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.graphql.dgs.client.codegen

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.TypeName
import graphql.language.VariableDefinition
import graphql.language.VariableReference

class EntitiesGraphQLQuery(representations: List<Any>) : GraphQLQuery() {

    val variables: Map<String, Any> = mapOf(REPRESENTATIONS_NAME to representations)

    init {
        variableDefinitions += VariableDefinition.newVariableDefinition()
            .name(REPRESENTATIONS_NAME)
            .type(
                NonNullType.newNonNullType(
                    ListType.newListType(
                        NonNullType.newNonNullType(
                            TypeName.newTypeName("_Any").build()
                        ).build()
                    ).build()
                ).build()
            )
            .build()
        input[REPRESENTATIONS_NAME] = VariableReference.newVariableReference().name(REPRESENTATIONS_NAME).build()
    }

    override fun getOperationName(): String {
        return "_entities"
    }

    class Builder {
        private val representations = mutableListOf<Any>()

        fun build(): EntitiesGraphQLQuery {
            return EntitiesGraphQLQuery(representations)
        }

        fun addRepresentationAsVariable(representation: Any): Builder {
            representations += mapper.convertValue(representation, HashMap::class.java)
            return this
        }
    }

    companion object {
        private val mapper = ObjectMapper()
        private const val REPRESENTATIONS_NAME = "representations"

        fun newRequest(): Builder {
            return Builder()
        }
    }
}
