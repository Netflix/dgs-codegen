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

import graphql.language.*

class GraphQLMultiQueryRequest(
    private val requests: List<GraphQLQueryRequest>
) {

    fun serialize(): String {
        if (requests.isEmpty()) throw AssertionError("Request must have at least one query")

        if (requests.size == 1) {
            return requests[0].serialize()
        }

        return multiQuery()
    }

    private fun multiQuery(): String {
        val operationDef = OperationDefinition.newOperationDefinition()
        requests[0].query.name?.let { operationDef.name(it) }
        requests[0].query.getOperationType()?.let { operationDef.operation(OperationDefinition.Operation.valueOf(it.uppercase())) }

        val queryType = requests[0].query.getOperationType().toString()
        val variableDefinitions = mutableListOf<VariableDefinition>()
        val selectionList: MutableList<Field.Builder> = mutableListOf()

        for (request in this.requests) {
            val query = request.query
            //Graphql only supports multiple mutations or multiple queries. Not a combination of the two.
            //Graphql does not support multiple subscriptions in one request http://spec.graphql.org/June2018/#sec-Single-root-field
            if (!query.getOperationType().equals(queryType) || queryType == OperationDefinition.Operation.SUBSCRIPTION.name) {
                throw AssertionError("Request has to have exclusively queries or mutations in a multi operation request")
            }

            if (request.query.variableDefinitions.isNotEmpty()) {
                variableDefinitions.addAll(request.query.variableDefinitions)
            }

            val selection = Field.newField(request.query.getOperationName())
            if (query.input.isNotEmpty()) {
                selection.arguments(
                    query.input.map { (name, value) ->
                        Argument(name, request.inputValueSerializer.toValue(value))
                    }
                )
            }

            if (request.projection != null) {
                val selectionSet = if (request.projection is BaseSubProjectionNode<*, *>) {
                    request.projectionSerializer.toSelectionSet(request.projection.root() as BaseProjectionNode)
                } else {
                    request.projectionSerializer.toSelectionSet(request.projection)
                }
                if (selectionSet.selections.isNotEmpty()) {
                    selection.selectionSet(selectionSet)
                }
            }
            if (query.queryAlias.isNotEmpty()) {
                selection.alias(query.queryAlias)
            }

            selectionList.add(selection)
        }

        operationDef.selectionSet(SelectionSet.newSelectionSet(selectionList.map(Field.Builder::build).toList()).build())

        return AstPrinter.printAst(operationDef.build())
    }
}
