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

import com.squareup.kotlinpoet.TypeName
import graphql.language.Description
import graphql.language.Document
import graphql.language.EnumTypeDefinition
import graphql.language.ImplementingTypeDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.NamedNode
import graphql.language.ObjectTypeDefinition
import graphql.language.UnionTypeDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal data class Field(
    val name: String,
    val type: TypeName,
    val description: Description?,
)

/**
 * Returns a map of interface name to list of field names for all interfaces in the document
 */
internal fun Document.interfaceFields(): Map<String, List<String>> {
    return getDefinitionsOfType(InterfaceTypeDefinition::class.java)
        .associate { i -> i.name to i.fieldDefinitions.map { it.name } }
}

/**
 * Returns a map of enum name to list of field names for all enums in the document
 */
internal fun Document.enumFields(): Map<String, List<String>> {
    return getDefinitionsOfType(EnumTypeDefinition::class.java)
        .associate { i -> i.name to i.enumValueDefinitions.map { it.name } }
}

/**
 * Returns the list of interfaces that this type implements
 */
internal fun ImplementingTypeDefinition<*>.implementedInterfaces(): List<String> {
    return implements
        .filterIsInstance<NamedNode<*>>()
        .map { it.name }
}

/**
 * Returns the set of fields that should be overridden
 */
internal fun overrideFields(
    interfaceFields: Map<String, List<String>>,
    implementedInterfaces: List<String>,
): Set<String> {
    return implementedInterfaces
        .mapNotNull { interfaceFields[it] }
        .flatten()
        .toSet()
}

internal fun Document.invertedUnionLookup(): Map<String, List<String>> {
    return getDefinitionsOfType(UnionTypeDefinition::class.java)
        .flatMap { u -> u.memberTypes.filterIsInstance<NamedNode<*>>().map { m -> m.name to u.name } }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
}

internal fun Document.invertedInterfaceLookup(): Map<String, List<String>> {
    return getDefinitionsOfType(InterfaceTypeDefinition::class.java)
        .plus(getDefinitionsOfType(ObjectTypeDefinition::class.java))
        .flatMap { o -> o.implements.filterIsInstance<NamedNode<*>>().map { i -> i.name to o.name } }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
}
