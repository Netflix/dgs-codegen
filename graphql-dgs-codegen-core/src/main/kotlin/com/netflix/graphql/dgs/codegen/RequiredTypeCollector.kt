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

import graphql.language.*
import graphql.util.TraversalControl
import graphql.util.TraverserContext

class RequiredTypeCollector(
    private val document: Document,
    queries: Set<String> = emptySet(),
    mutations: Set<String> = emptySet()
) {
    val requiredTypes: Set<String> = LinkedHashSet()

    init {
        val fieldDefinitions = mutableListOf<FieldDefinition>()
        for (definition in document.definitions.asSequence().filterIsInstance<ObjectTypeDefinition>()) {
            when (definition.name) {
                "Query" -> definition.fieldDefinitions.filterTo(fieldDefinitions) { it.name in queries }
                "Mutation" -> definition.fieldDefinitions.filterTo(fieldDefinitions) { it.name in mutations }
            }
        }

        val required = requiredTypes as MutableSet<String>

        NodeTraverser().postOrder(
            object : NodeVisitorStub() {
                val visitedTypes = mutableSetOf<String>()

                override fun visitInputObjectTypeDefinition(
                    node: InputObjectTypeDefinition,
                    context: TraverserContext<Node<Node<*>>>
                ): TraversalControl {
                    required += node.name

                    node.inputValueDefinitions.filter { !visitedTypes.contains(it.name) }.forEach {
                        visitedTypes.add(it.name)
                        it.type.findTypeDefinition(document)?.accept(context, this)
                    }
                    return TraversalControl.CONTINUE
                }

                override fun visitEnumTypeDefinition(
                    node: EnumTypeDefinition,
                    context: TraverserContext<Node<Node<*>>>
                ): TraversalControl {
                    required += node.name
                    return TraversalControl.CONTINUE
                }

                override fun visitInputValueDefinition(
                    node: InputValueDefinition,
                    context: TraverserContext<Node<Node<*>>>
                ): TraversalControl {
                    node.type.findTypeDefinition(document)?.accept(context, this)
                    return TraversalControl.CONTINUE
                }
            },
            fieldDefinitions
        )
    }
}
