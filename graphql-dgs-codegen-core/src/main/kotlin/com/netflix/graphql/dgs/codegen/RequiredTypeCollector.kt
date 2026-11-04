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
import graphql.language.EnumTypeDefinition
import graphql.language.FieldDefinition
import graphql.language.InputObjectTypeDefinition
import graphql.language.InputValueDefinition
import graphql.language.Node
import graphql.language.NodeTraverser
import graphql.language.NodeVisitorStub
import graphql.language.ObjectTypeDefinition
import graphql.language.OperationDefinition.Operation
import graphql.language.TypeName
import graphql.language.UnionTypeDefinition
import graphql.util.TraversalControl
import graphql.util.TraverserContext

class RequiredTypeCollector internal constructor(
    private val schemaIndex: SchemaIndex,
    config: CodeGenConfig,
) {
    constructor(document: Document, config: CodeGenConfig) : this(SchemaIndex(document), config)

    val requiredTypes: Set<String> = LinkedHashSet()

    init {
        val fieldDefinitions = mutableListOf<FieldDefinition>()
        for (definition in schemaIndex.definitions.asSequence().filterIsInstance<ObjectTypeDefinition>()) {
            val operations = schemaIndex.operationTypes[definition.name].orEmpty()
            val needsNestedProjection =
                config.generateKotlinClosureProjections && definition.name in schemaIndex.nestedOperationTypes
            if (config.generateClientApi && (operations.isEmpty() || needsNestedProjection)) {
                fieldDefinitions.addAll(definition.fieldDefinitions)
            }
            for (operation in operations) {
                val included =
                    when (operation) {
                        Operation.QUERY -> config.includeQueries
                        Operation.MUTATION -> config.includeMutations
                        Operation.SUBSCRIPTION -> config.includeSubscriptions
                    }
                // Empty include sets select all fields for clients, but no fields for type-only generation.
                definition.fieldDefinitions.filterTo(fieldDefinitions) {
                    (config.generateClientApi && included.isEmpty()) || it.name in included
                }
            }
        }

        val required = requiredTypes as MutableSet<String>

        NodeTraverser().postOrder(
            object : NodeVisitorStub() {
                val visitedTypes = mutableSetOf<String>()

                override fun visitObjectTypeDefinition(
                    node: ObjectTypeDefinition,
                    context: TraverserContext<Node<*>>,
                ): TraversalControl {
                    node.fieldDefinitions
                        .flatMap { it.inputValueDefinitions }
                        .forEach {
                            it.type.findTypeDefinition(schemaIndex)?.accept(context, this)
                        }

                    node.fieldDefinitions
                        .filter { !visitedTypes.contains("${node.name}.${it.name}") }
                        .forEach {
                            visitedTypes.add("${node.name}.${it.name}")
                            it.type.findTypeDefinition(schemaIndex)?.accept(context, this)
                        }
                    return TraversalControl.CONTINUE
                }

                override fun visitUnionTypeDefinition(
                    node: UnionTypeDefinition,
                    context: TraverserContext<Node<*>>,
                ): TraversalControl {
                    node.memberTypes.forEach {
                        it.findTypeDefinition(schemaIndex)?.accept(context, this)
                    }
                    return TraversalControl.CONTINUE
                }

                override fun visitTypeName(
                    node: TypeName,
                    context: TraverserContext<Node<*>>,
                ): TraversalControl {
                    node.findTypeDefinition(schemaIndex)?.accept(context, this)
                    return TraversalControl.CONTINUE
                }

                override fun visitInputObjectTypeDefinition(
                    node: InputObjectTypeDefinition,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl {
                    required += node.name

                    node.inputValueDefinitions
                        .filter { !visitedTypes.contains("${node.name}.${it.name}") }
                        .forEach {
                            visitedTypes.add("${node.name}.${it.name}")
                            it.type.findTypeDefinition(schemaIndex)?.accept(context, this)
                        }
                    return TraversalControl.CONTINUE
                }

                override fun visitEnumTypeDefinition(
                    node: EnumTypeDefinition,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl {
                    required += node.name
                    return TraversalControl.CONTINUE
                }

                override fun visitInputValueDefinition(
                    node: InputValueDefinition,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl {
                    node.type.findTypeDefinition(schemaIndex)?.accept(context, this)
                    return TraversalControl.CONTINUE
                }
            },
            fieldDefinitions,
        )
    }
}
