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
    val requiredTypes: Set<String>

    init {
        val queryFieldDefinitions: List<FieldDefinition> = document.definitions.filterIsInstance<ObjectTypeDefinition>()
            .find { it.name == "Query" }?.fieldDefinitions ?: emptyList()

        val mutationDefinitions: List<FieldDefinition> = document.definitions.filterIsInstance<ObjectTypeDefinition>()
            .find { it.name == "Mutation" }?.fieldDefinitions ?: emptyList()
        val fieldDefinitions = queryFieldDefinitions.plus(mutationDefinitions).filter { queries.contains(it.name) || mutations.contains(it.name) }

        val nodeTraverserResult = NodeTraverser().postOrder(object : NodeVisitorStub() {
            override fun visitInputObjectTypeDefinition(
                node: InputObjectTypeDefinition,
                context: TraverserContext<Node<Node<*>>>
            ): TraversalControl {
                println(node)

                val currentAccumulate = context.getNewAccumulate<Set<String>>()
                if (currentAccumulate == null) {
                    context.setAccumulate(setOf(node.name))
                } else {
                    context.setAccumulate(currentAccumulate.plus(node.name))
                }

                node.inputValueDefinitions.map { it.type.findTypeDefinition(document)?.accept(context, this) }

                return TraversalControl.CONTINUE
            }

            override fun visitEnumTypeDefinition(
                node: EnumTypeDefinition,
                context: TraverserContext<Node<Node<*>>>
            ): TraversalControl {
                println(node)


                val currentAccumulate = context.getNewAccumulate<Set<String>>()
                if (currentAccumulate == null) {
                    context.setAccumulate(setOf(node.name))
                } else {
                    context.setAccumulate(currentAccumulate.plus(node.name))
                }

                return TraversalControl.CONTINUE
            }

            override fun visitInputValueDefinition(
                node: InputValueDefinition,
                context: TraverserContext<Node<Node<*>>>
            ): TraversalControl {
                println(node)

                node.type.findTypeDefinition(document)?.accept(context, this)


                return super.visitInputValueDefinition(node, context)
            }
        }, fieldDefinitions)

        requiredTypes = if (nodeTraverserResult != null && nodeTraverserResult is Set<*>) {
            nodeTraverserResult as Set<String>
        } else {
            emptySet()
        }
    }
}