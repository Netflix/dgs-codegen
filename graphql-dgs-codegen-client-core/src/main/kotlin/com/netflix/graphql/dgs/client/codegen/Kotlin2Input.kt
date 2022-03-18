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

abstract class Kotlin2Input {

    companion object {

        private val inputSerializer = InputValueSerializer()

        protected fun inputToString(value: Any?): String {
            return inputSerializer.serialize(value)
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
