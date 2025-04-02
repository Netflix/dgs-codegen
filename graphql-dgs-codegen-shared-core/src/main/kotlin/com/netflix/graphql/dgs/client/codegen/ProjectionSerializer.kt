/*
 * Copyright 2021 Netflix, Inc.
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

import graphql.language.Argument
import graphql.language.AstPrinter
import graphql.language.Field
import graphql.language.InlineFragment
import graphql.language.SelectionSet
import graphql.language.TypeName

class ProjectionSerializer(
    private val inputValueSerializer: InputValueSerializerInterface,
) {
    fun toSelectionSet(projection: BaseProjectionNode): SelectionSet {
        val selectionSet = SelectionSet.newSelectionSet()

        for ((fieldName, value) in projection.fields) {
            val fieldSelection =
                Field
                    .newField()
                    .name(fieldName)
                    .arguments(
                        projection.inputArguments[fieldName].orEmpty().map { (argName, values) ->
                            Argument(argName, inputValueSerializer.toValue(values))
                        },
                    )
            if (value is BaseProjectionNode) {
                val fieldSelectionSet = toSelectionSet(value)
                if (fieldSelectionSet.selections.isNotEmpty()) {
                    fieldSelection.selectionSet(fieldSelectionSet)
                }
            } else if (value != null) {
                fieldSelection.selectionSet(
                    SelectionSet
                        .newSelectionSet()
                        .selection(Field.newField(value.toString()).build())
                        .build(),
                )
            }
            selectionSet.selection(fieldSelection.build())
        }

        for (fragment in projection.fragments) {
            val typeCondition =
                fragment.schemaType
                    .map { TypeName(it) }
                    .orElseGet {
                        val className =
                            fragment::class.simpleName
                                ?: throw AssertionError("Unable to determine class name for projection: $fragment")
                        TypeName(
                            className
                                .substringAfterLast("_")
                                .substringBefore("Projection"),
                        )
                    }

            selectionSet.selection(
                InlineFragment
                    .newInlineFragment()
                    .typeCondition(typeCondition)
                    .selectionSet(toSelectionSet(fragment))
                    .build(),
            )
        }
        return selectionSet.build()
    }

    fun serialize(projection: BaseProjectionNode): String = AstPrinter.printAst(toSelectionSet(projection))
}
