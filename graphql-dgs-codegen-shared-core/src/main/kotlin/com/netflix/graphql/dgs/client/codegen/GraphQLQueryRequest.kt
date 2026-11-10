/*
 * Copyright 2022 Netflix, Inc.
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

import graphql.GraphQLContext
import graphql.language.Argument
import graphql.language.AstPrinter
import graphql.language.Directive
import graphql.language.Field
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.OperationDefinition
import graphql.language.SelectionSet
import graphql.language.Type
import graphql.language.TypeName
import graphql.language.VariableDefinition
import graphql.language.VariableReference
import graphql.schema.Coercing

class GraphQLQueryRequest
    @JvmOverloads
    constructor(
        val query: GraphQLQuery,
        val projection: BaseProjectionNode? = null,
        options: GraphQLQueryRequestOptions? = null,
    ) {
        private var selectionSet: SelectionSet? = null
        private val operationFieldDirectives = options?.operationFieldDirectives?.toList() ?: emptyList()
        constructor(
            query: GraphQLQuery,
            projection: BaseProjectionNode,
            scalars: Map<Class<*>, Coercing<*, *>>,
        ) : this(query = query, projection = projection, options = GraphQLQueryRequestOptions(scalars = scalars))
        constructor(
            query: GraphQLQuery,
            selectionSet: SelectionSet,
            scalars: Map<Class<*>, Coercing<*, *>>? = null,
        ) : this(
            query = query,
            projection = null,
            options =
                GraphQLQueryRequestOptions(
                    scalars =
                        scalars ?: emptyMap(),
                ),
        ) {
            this.selectionSet = selectionSet
        }

        class GraphQLQueryRequestOptions(
            val scalars: Map<Class<*>, Coercing<*, *>> = emptyMap(),
            val graphQLContext: GraphQLContext = GraphQLContext.getDefault(),
        ) {
            /**
             * Directives on the top-level selection field, restricted to directives declared `on FIELD` in the schema.
             * Each request copies this list at construction so later changes do not affect that request.
             */
            var operationFieldDirectives: List<Directive> = emptyList()

            // When enabled, input values that are derived from properties
            // whose values are null will be serialized in the query request
            var allowNullablePropertyInputValues = false
        }

        val inputValueSerializer =
            if (options?.allowNullablePropertyInputValues == true) {
                NullableInputValueSerializer(options.scalars)
            } else {
                InputValueSerializer(
                    options?.scalars ?: emptyMap(),
                    options?.graphQLContext ?: GraphQLContext.getDefault(),
                )
            }

        val projectionSerializer = ProjectionSerializer(inputValueSerializer, query)

        fun serialize(): String = serialize(false)

        fun serializeCompact(): String = serialize(true)

        private fun serialize(compact: Boolean): String {
            val operationDef = OperationDefinition.newOperationDefinition()
            val variableDefinitions = query.variableDefinitions.toMutableList()

            query.name?.let { operationDef.name(it) }
            query
                .getOperationType()
                ?.let { operationDef.operation(OperationDefinition.Operation.valueOf(it.uppercase())) }

            val selection = toField(variableDefinitions)
            if (variableDefinitions.isNotEmpty()) {
                operationDef.variableDefinitions(mergeVariableDefinitions(variableDefinitions))
            }

            operationDef.selectionSet(SelectionSet.newSelectionSet().selection(selection).build())

            return if (compact) {
                AstPrinter.printAstCompact(operationDef.build())
            } else {
                AstPrinter.printAst(operationDef.build())
            }
        }

        internal fun toField(variableDefinitions: MutableList<VariableDefinition>): Field {
            val selection = Field.newField(query.getOperationName())
            if (query.input.isNotEmpty()) {
                selection.arguments(
                    query.input.map { (name, value) ->
                        if (query.variableReferences.containsKey(name)) {
                            Argument(name, VariableReference.of(query.variableReferences[name]))
                        } else {
                            Argument(name, inputValueSerializer.toValue(value))
                        }
                    },
                )
            }

            selection.directives(operationFieldDirectives)

            if (projection != null) {
                val selectionSetFromProjection =
                    if (projection is BaseSubProjectionNode<*, *> && projection.root() != null) {
                        projectionSerializer.toSelectionSet(projection.root() as BaseProjectionNode, variableDefinitions)
                    } else {
                        projectionSerializer.toSelectionSet(projection, variableDefinitions)
                    }
                if (selectionSetFromProjection.selections.isNotEmpty()) {
                    selection.selectionSet(selectionSetFromProjection)
                }
            }

            if (selectionSet != null) {
                selection.selectionSet(selectionSet)
            }

            return selection.build()
        }
    }

@JvmSynthetic
internal fun mergeVariableDefinitions(definitions: List<VariableDefinition>): List<VariableDefinition> =
    definitions
        .groupBy { definition ->
            Triple(
                definition.name,
                definition.defaultValue?.let { AstPrinter.printAstCompact(it) },
                definition.directives.map { AstPrinter.printAstCompact(it) },
            )
        }.values
        .flatMap { group ->
            var type: Type<*> = group.first().type
            for (definition in group.drop(1)) {
                // Conflicting types or list shapes must remain visible to server validation.
                type = mergeVariableTypes(type, definition.type) ?: return@flatMap group.distinctBy { AstPrinter.printAstCompact(it) }
            }
            listOf(group.first().transform { it.type(type) })
        }

private fun mergeVariableTypes(
    first: Type<*>,
    second: Type<*>,
): Type<*>? {
    val firstType = if (first is NonNullType) first.type else first
    val secondType = if (second is NonNullType) second.type else second
    val merged =
        when {
            firstType is TypeName && secondType is TypeName && firstType.name == secondType.name -> {
                firstType
            }

            firstType is ListType && secondType is ListType -> {
                ListType(mergeVariableTypes(firstType.type, secondType.type) ?: return null)
            }

            else -> {
                return null
            }
        }
    // A non-null variable can also satisfy a nullable argument, including within lists.
    return if (first is NonNullType || second is NonNullType) NonNullType(merged) else merged
}
