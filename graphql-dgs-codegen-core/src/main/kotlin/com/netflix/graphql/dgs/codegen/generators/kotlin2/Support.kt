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

package com.netflix.graphql.dgs.codegen.generators.kotlin2

import graphql.language.Document
import graphql.language.ImplementingTypeDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.NamedNode

/**
 * Returns a map of interface name to list of field names for all interfaces in the document
 */
internal fun interfaceFields(document: Document): Map<String, List<String>> {
    return document
        .getDefinitionsOfType(InterfaceTypeDefinition::class.java)
        .associate { i -> i.name to i.fieldDefinitions.map { it.name } }
}

/**
 * Returns the list of interfaces that this type implements
 */
internal fun implementedInterfaces(typeDefinition: ImplementingTypeDefinition<*>): List<String> {
    return typeDefinition
        .implements
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
