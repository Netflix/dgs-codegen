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

@DslMarker
annotation class QueryProjectionMarker

object DefaultTracker {

    // set of what defaults have been used
    val defaults: ThreadLocal<MutableSet<String>> = ThreadLocal.withInitial { mutableSetOf() }

    // add a default value
    fun add(arg: String) {
        defaults.get().add(arg)
    }

    // consume the set defaults & reset
    fun getAndClear(): Set<String> {
        try {
            return defaults.get()
        } finally {
            defaults.set(mutableSetOf())
        }
    }
}

@QueryProjectionMarker
abstract class GraphQLProjection(defaultFields: Set<String> = setOf("__typename")) {

    companion object {

        private val inputSerializer = InputValueSerializer()

        @JvmStatic
        protected fun <T> default(arg: String): T? {
            DefaultTracker.add(arg)
            return null
        }
    }

    private val builder = StringBuilder("{ ${defaultFields.joinToString(" ")} ")

    protected fun field(field: String) {
        builder.append("$field ")
    }

    protected fun <T : GraphQLProjection> project(field: String, projection: T, projectionFields: T.() -> T) {
        builder.append("$field ")
        projectionFields.invoke(projection)
        builder.append(projection.asQuery())
    }

    fun asQuery() = "$builder}"

    protected fun formatArgs(vararg args: Pair<String, Any?>): String {
        val defaults = DefaultTracker.getAndClear()
        return args
            .filter { (k, _) -> !defaults.contains(k) }
            .joinToString(", ") { (k, v) -> "$k: ${inputSerializer.serialize(v)}" }
    }
}

abstract class GraphQLInput : InputValue {

    companion object {

        @JvmStatic
        protected fun <T> default(arg: String): T? {
            DefaultTracker.add(arg)
            return null
        }
    }

    private val defaults = DefaultTracker.getAndClear()

    abstract fun fields(): List<Pair<String, Any?>>

    override fun inputValues(): List<Pair<String, Any?>> {
        return fields().filter { (k, _) -> !defaults.contains(k) }
    }
}
