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

import java.util.*

abstract class BaseSubProjectionNode<S, T, R>(
    val parent: T,
    val root: R,
    schemaType: Optional<String> = Optional.empty()
) : BaseProjectionNode(schemaType) {

    constructor(parent: T, root: R) : this(parent, root, schemaType = Optional.empty())
    fun parent(): T {
        return parent
    }

    fun root(): R {
        return root
    }

    fun alias(alias: String, assigner: (node: S) -> Any): S {
        // Avoid alias against existing field name
        if (this.javaClass.kotlin.members.any { it.name.equals(alias, ignoreCase = true) }) {
            throw AssertionError("Tried to specify alias $alias which already exists as field.")
        }

        // Stash our current set of fields
        val currentFields = fields.toMap()
        val currentInputArguments = inputArguments.toMap()
        fields.clear()
        inputArguments.clear()

        // Track the aliased field
        @Suppress("UNCHECKED_CAST")
        assigner(this as S)

        // Constraints on how aliases are assigned
        if (fields.isEmpty()) {
            throw AssertionError("Tried to initialize alias but did not call any fields.")
        }

        if (fields.size > 1) {
            throw AssertionError("Tried to call multiple fields while initializing alias.")
        }

        val fieldName = fields.keys.first()
        val fieldValue = fields.values.first()
        val fieldArguments = mapOf(alias to inputArguments[fieldName]?.let { it }.orEmpty())

        // Restore our fields stash
        fields.clear()
        fields.putAll(currentFields)
        inputArguments.clear()
        inputArguments.putAll(currentInputArguments)

        // Layer in our new state
        aliases[alias] = FieldAlias(fieldName = fieldName, value = fieldValue)
        inputArguments.putAll(fieldArguments)

        return this
    }
}
