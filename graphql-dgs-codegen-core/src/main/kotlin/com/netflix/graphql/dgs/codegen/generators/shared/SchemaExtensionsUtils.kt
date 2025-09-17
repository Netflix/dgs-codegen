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

import com.netflix.graphql.dgs.codegen.fieldDefinitions
import graphql.language.Definition
import graphql.language.EnumTypeExtensionDefinition
import graphql.language.FieldDefinition
import graphql.language.InputObjectTypeExtensionDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.InterfaceTypeExtensionDefinition
import graphql.language.NamedNode
import graphql.language.ObjectTypeDefinition
import graphql.language.ObjectTypeExtensionDefinition
import graphql.language.TypeDefinition
import graphql.language.UnionTypeExtensionDefinition

object SchemaExtensionsUtils {
    fun findTypeExtensions(
        name: String,
        definitions: Collection<Definition<*>>,
    ) = findExtensions<ObjectTypeExtensionDefinition>(name, definitions)

    fun findInputExtensions(
        name: String,
        definitions: Collection<Definition<*>>,
    ) = findExtensions<InputObjectTypeExtensionDefinition>(name, definitions)

    fun findEnumExtensions(
        name: String,
        definitions: Collection<Definition<*>>,
    ) = findExtensions<EnumTypeExtensionDefinition>(name, definitions)

    fun findInterfaceExtensions(
        name: String,
        definitions: Collection<Definition<*>>,
    ) = findExtensions<InterfaceTypeExtensionDefinition>(name, definitions)

    fun findUnionExtensions(
        name: String,
        definitions: Collection<Definition<*>>,
    ) = findExtensions<UnionTypeExtensionDefinition>(name, definitions)

    private inline fun <reified R : NamedNode<*>> findExtensions(
        name: String,
        definitions: Collection<Definition<*>>,
    ) = definitions
        .asSequence()
        .filterIsInstance<R>()
        .filter { name == it.name }
        .toList()

    /**
     * Collects all field definitions for a given type, including fields from:
     * - The type definition itself
     * - Extension definitions (extend type/interface Foo)
     * - Duplicate type definitions (multiple type/interface Foo blocks)
     *
     * Fields are deduplicated by name, with the first occurrence taking precedence.
     */
    fun collectAllFieldDefinitions(
        type: TypeDefinition<*>,
        definitions: Collection<Definition<*>>,
    ): List<FieldDefinition> =
        when (type) {
            is ObjectTypeDefinition -> {
                val extensionFields = findTypeExtensions(type.name, definitions).flatMap { it.fieldDefinitions }
                val duplicateTypeFields =
                    definitions
                        .filterIsInstance<ObjectTypeDefinition>()
                        .filter { it != type && it.name == type.name }
                        .flatMap { it.fieldDefinitions }
                (type.fieldDefinitions + extensionFields + duplicateTypeFields).distinctBy { it.name }
            }
            is InterfaceTypeDefinition -> {
                val extensionFields = findInterfaceExtensions(type.name, definitions).flatMap { it.fieldDefinitions }
                val duplicateTypeFields =
                    definitions
                        .filterIsInstance<InterfaceTypeDefinition>()
                        .filter { it != type && it.name == type.name }
                        .flatMap { it.fieldDefinitions }
                (type.fieldDefinitions + extensionFields + duplicateTypeFields).distinctBy { it.name }
            }
            else -> type.fieldDefinitions()
        }
}

fun <T> Collection<T>.excludeSchemaTypeExtension(): Collection<T> = this.filter { !isSchemaTypeExtension(it) }

fun <T> Sequence<T>.excludeSchemaTypeExtension(): Sequence<T> = this.filter { !isSchemaTypeExtension(it) }

private fun <T> isSchemaTypeExtension(it: T): Boolean =
    when (it) {
        is InputObjectTypeExtensionDefinition -> true
        is ObjectTypeExtensionDefinition -> true
        is EnumTypeExtensionDefinition -> true
        is InterfaceTypeExtensionDefinition -> true
        is UnionTypeExtensionDefinition -> true
        else -> false
    }
