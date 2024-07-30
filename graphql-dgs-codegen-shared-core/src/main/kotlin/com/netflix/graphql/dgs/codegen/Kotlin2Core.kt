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

import com.netflix.graphql.dgs.client.codegen.InputValue
import com.netflix.graphql.dgs.client.codegen.InputValueSerializer
import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import graphql.language.Argument
import graphql.language.AstPrinter
import graphql.language.Document
import graphql.language.Field
import graphql.language.InlineFragment
import graphql.language.OperationDefinition
import graphql.language.SelectionSet
import graphql.language.TypeName

@DslMarker
annotation class QueryProjectionMarker

object DefaultTracker {

    // set of what defaults have been used
    val defaults: ThreadLocal<MutableMap<String, MutableSet<String>>> = ThreadLocal.withInitial { mutableMapOf() }

    // add a default value
    fun add(type: String, arg: String) {
        defaults.get()
            .computeIfAbsent(type) { mutableSetOf() }
            .add(arg)
    }

    // consume the set defaults & reset them
    fun getAndClear(type: String): Set<String> {
        return defaults.get().remove(type) ?: emptySet()
    }
}

@QueryProjectionMarker
abstract class GraphQLProjection(
    protected val inputValueSerializer: InputValueSerializerInterface? = null,
    defaultFields: Set<String> = setOf("__typename")
) {

    companion object {

        val defaultInputValueSerializer = InputValueSerializer()

        @JvmStatic
        protected fun <T> default0(arg: String): T? {
            DefaultTracker.add(this::class.qualifiedName!!, arg)
            return null
        }

        @JvmStatic
        protected inline fun <reified TClass, reified TValue> default(arg: String): TValue? {
            DefaultTracker.add(TClass::class.qualifiedName!!, arg)
            return null
        }

        fun <T : GraphQLProjection> asQuery(
            operation: OperationDefinition.Operation,
            projection: T,
            projectionFields: T.() -> T
        ): String {
            projectionFields.invoke(projection)

            val document = Document.newDocument()
                .definition(
                    OperationDefinition.newOperationDefinition()
                        .operation(operation)
                        .selectionSet(projection.builder.build())
                        .build()
                )
                .build()

            return AstPrinter.printAst(document)
        }
    }

    private val builder = SelectionSet.newSelectionSet()

    init {
        // add default fields
        defaultFields.forEach { field(it) }
    }

    private fun arguments(
        args: List<Pair<String, Any?>>,
        defaults: Set<String>
    ): List<Argument> {
        return args
            .filter { (k, _) -> !defaults.contains(k) }
            .map { (arg, value) ->
                Argument.newArgument()
                    .name(arg)
                    .value((inputValueSerializer ?: defaultInputValueSerializer).toValue(value))
                    .build()
            }
    }

    protected fun field(
        name: String,
        vararg args: Pair<String, Any?>
    ) {
        val defaults = DefaultTracker.getAndClear(this::class.qualifiedName!!)
        builder.selection(
            Field.newField()
                .name(name)
                .arguments(arguments(args.toList(), defaults))
                .build()
        )
    }

    protected fun <T : GraphQLProjection> field(
        alias: String? = null,
        name: String,
        projection: T,
        projectionFields: T.() -> T,
        vararg args: Pair<String, Any?>
    ) {
        val defaults = DefaultTracker.getAndClear(this::class.qualifiedName!!)
        projectionFields.invoke(projection)

        val fieldBuilder = Field.newField()
            .name(name)
            .arguments(arguments(args.toList(), defaults))
            .selectionSet(projection.builder.build())

        alias?.also { fieldBuilder.alias(it) }

        builder.selection(fieldBuilder.build())
    }

    protected fun <T : GraphQLProjection> fragment(
        name: String,
        projection: T,
        projectionFields: T.() -> T
    ) {
        projectionFields.invoke(projection)
        builder.selection(
            InlineFragment.newInlineFragment()
                .typeCondition(TypeName(name))
                .selectionSet(projection.builder.build())
                .build()
        )
    }
}

abstract class GraphQLInput : InputValue {

    companion object {

        @JvmStatic
        protected fun <T> default0(arg: String): T? {
            DefaultTracker.add(this::class.qualifiedName!!, arg)
            return null
        }

        @JvmStatic
        protected inline fun <reified TClass, reified TValue> default(arg: String, defaultValue: TValue): TValue {
            DefaultTracker.add(TClass::class.qualifiedName!!, arg)
            return defaultValue
        }
    }

    private val defaults = DefaultTracker.getAndClear(this::class.qualifiedName!!)

    abstract fun fields(): List<Pair<String, Any?>>

    override fun inputValues(): List<Pair<String, Any?>> {
        return fields().filter { (k, _) -> !defaults.contains(k) }
    }
}
