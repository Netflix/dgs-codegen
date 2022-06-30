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

@DslMarker
annotation class QueryProjectionMarker

@QueryProjectionMarker
abstract class GraphQLProjection(defaultFields: Set<String> = setOf("__typename")) : GraphQLInput() {

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
}

abstract class GraphQLInput {

    companion object {

        private val inputSerializer = InputValueSerializer()

        protected fun inputToString(value: Any?): String {
            // TODO escape newlines in InputValueSerializer
            return inputSerializer.serialize(value).replace("\n", "\\n")
        }

        val defaults: ThreadLocal<MutableSet<String>> = ThreadLocal.withInitial { mutableSetOf() }

        @JvmStatic
        protected fun <T> default(arg: String): T? {
            defaults.get().add(arg)
            return null
        }
    }

    private val _defaults = defaults.get()

    init {
        defaults.set(mutableSetOf())
    }

    protected fun formatArgs(vararg args: Pair<String, Any?>): String {
        return args
            .filter { (k, _) -> !_defaults.contains(k) }
            .joinToString(", ") { (k, v) -> "$k: ${inputToString(v)}" }
    }
}
