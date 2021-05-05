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

package com.netflix.graphql.dgs.codegen.generators.shared

import graphql.language.*

object SchemaExtensionsUtils {
    fun findTypeExtensions(name: String, definitions: Collection<Definition<*>>) =
        findExtensions<ObjectTypeExtensionDefinition>(name, definitions)

    fun findInputExtensions(name: String, definitions: Collection<Definition<*>>) =
        findExtensions<InputObjectTypeExtensionDefinition>(name, definitions)

    fun findEnumExtensions(name: String, definitions: Collection<Definition<*>>) =
        findExtensions<EnumTypeExtensionDefinition>(name, definitions)

    fun findInterfaceExtensions(name: String, definitions: Collection<Definition<*>>) =
        findExtensions<InterfaceTypeExtensionDefinition>(name, definitions)

    fun findUnionExtensions(name: String, definitions: Collection<Definition<*>>) =
        findExtensions<UnionTypeExtensionDefinition>(name, definitions)

    private inline fun <reified R : NamedNode<*>> findExtensions(name: String, definitions: Collection<Definition<*>>) =
        definitions.asSequence()
            .filterIsInstance<R>()
            .filter { name == it.name }
            .toList()
}

fun <T> List<T>.filterSchemaTypeExtensions(): List<T> {
    return this.filter { !isSchemaTypeExtension(it) }
}

fun <T> Sequence<T>.filterSchemaTypeExtensions(): Sequence<T> {
    return this.filter { !isSchemaTypeExtension(it) }
}

private fun <T> isSchemaTypeExtension(it: T): Boolean {
    return when (it) {
        is InputObjectTypeExtensionDefinition -> true
        is ObjectTypeExtensionDefinition -> true
        is EnumTypeExtensionDefinition -> true
        is InterfaceTypeExtensionDefinition -> true
        is UnionTypeExtensionDefinition -> true
        else -> false
    }
}
