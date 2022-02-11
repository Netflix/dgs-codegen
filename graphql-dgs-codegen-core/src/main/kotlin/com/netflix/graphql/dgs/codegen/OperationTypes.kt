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

import graphql.language.Document
import graphql.language.SchemaDefinition

object OperationTypes {
    fun initialize(document: Document) {
        restoreDefaults()
        val schemaDefinitionList = document.definitions.filterIsInstance<SchemaDefinition>()

        if (schemaDefinitionList.isNotEmpty()) {
            val schemaDefinition = schemaDefinitionList.last()
            schemaDefinition.operationTypeDefinitions
                .forEach {
                    when (it.name) {
                        "query" -> query = it.typeName.name
                        "mutation" -> mutation = it.typeName.name
                        "subscription" -> subscription = it.typeName.name
                    }
                }
        }
    }

    private fun restoreDefaults() {
        query = "Query"
        mutation = "Mutation"
        subscription = "Subscription"
    }

    fun isOperationType(typeName: String) = typeName == query || typeName == mutation || typeName == subscription

    var query = "Query"
    var mutation = "Mutation"
    var subscription = "Subscription"
}