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

package com.netflix.graphql.dgs.client.codegen

import graphql.language.OperationDefinition

class GraphQLMultiQueryRequest(
    private val requests: List<GraphQLQueryRequest>
) {

    fun serialize(): String {
        if (requests.isEmpty()) throw AssertionError("Request must have at least one query")

        if (requests.size == 1) {
            return requests[0].serialize()
        }

        return mergeMultipleQueries()
    }

    private fun mergeMultipleQueries(): String {

        val sb = StringBuilder()
        val queryType = requests[0].query.getOperationType().toString()
        sb.append("$queryType {\n")

        for (request in requests) {
            if (request.query.getOperationType() != queryType || queryType == OperationDefinition.Operation.SUBSCRIPTION.name)
                throw AssertionError("Can only have exclusively queries or mutations in multi operation request")

            sb.append(" ")
            if (request.query.queryAlias.isNotEmpty())
                sb.append(request.query.queryAlias).append(":")
            sb.append(
                stripOperationTypeAndBrackets(
                    request.serialize(),
                    queryType
                )
            )
        }
        sb.append("}")

        return sb.toString()
    }

    private fun stripOperationTypeAndBrackets(query: String, queryType: String): String {
        return query.removePrefix("$queryType {\n ")
            .removeSuffix("}");
    }
}