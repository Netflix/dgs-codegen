/*
 * Copyright 2026 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.graphql.dgs.codegen

import com.netflix.graphql.dgs.codegen.generators.shared.JAVA_TYPE_DIRECTIVE_NAME
import graphql.language.Definition
import graphql.language.Document
import graphql.language.EnumTypeExtensionDefinition
import graphql.language.InputObjectTypeExtensionDefinition
import graphql.language.InterfaceTypeExtensionDefinition
import graphql.language.NamedNode
import graphql.language.ObjectTypeDefinition
import graphql.language.ObjectTypeExtensionDefinition
import graphql.language.ScalarTypeDefinition
import graphql.language.StringValue
import graphql.language.TypeDefinition
import graphql.language.UnionTypeExtensionDefinition

/** Immutable, document-order-preserving indexes over a parsed schema. */
internal class SchemaIndex(
    internal val document: Document,
) {
    internal val definitions: List<Definition<*>> = document.definitions

    private val definitionsByName: Map<String, List<TypeDefinition<*>>> =
        definitions.filterIsInstance<TypeDefinition<*>>().groupBy { it.name }

    private val objectExtensions = index<ObjectTypeExtensionDefinition>(definitions)
    private val inputExtensions = index<InputObjectTypeExtensionDefinition>(definitions)
    private val enumExtensions = index<EnumTypeExtensionDefinition>(definitions)
    private val interfaceExtensions = index<InterfaceTypeExtensionDefinition>(definitions)
    private val unionExtensions = index<UnionTypeExtensionDefinition>(definitions)

    private val implementationsByInterface: Map<String, List<ObjectTypeDefinition>> =
        definitions
            .filterIsInstance<ObjectTypeDefinition>()
            .flatMap { definition ->
                definition.implements
                    .filterIsInstance<NamedNode<*>>()
                    .map { it.name }
                    .distinct()
                    .map { it to definition }
            }.groupBy({ it.first }, { it.second })

    fun definitions(name: String): List<TypeDefinition<*>> = definitionsByName[name].orEmpty()

    fun typeExtensions(name: String): List<ObjectTypeExtensionDefinition> = objectExtensions[name].orEmpty()

    fun inputExtensions(name: String): List<InputObjectTypeExtensionDefinition> = inputExtensions[name].orEmpty()

    fun enumExtensions(name: String): List<EnumTypeExtensionDefinition> = enumExtensions[name].orEmpty()

    fun interfaceExtensions(name: String): List<InterfaceTypeExtensionDefinition> = interfaceExtensions[name].orEmpty()

    fun unionExtensions(name: String): List<UnionTypeExtensionDefinition> = unionExtensions[name].orEmpty()

    fun implementations(interfaceName: String): List<ObjectTypeDefinition> = implementationsByInterface[interfaceName].orEmpty()

    fun schemaTypeMapping(typeName: String): String? {
        val scalar =
            definitions(typeName)
                .filterIsInstance<ScalarTypeDefinition>()
                .firstOrNull { it.hasDirective(JAVA_TYPE_DIRECTIVE_NAME) }
                ?: return null
        val directive =
            scalar.getDirectives(JAVA_TYPE_DIRECTIVE_NAME).singleOrNull()
                ?: throw IllegalArgumentException("multiple @$JAVA_TYPE_DIRECTIVE_NAME directives are defined")
        val nameArgument =
            directive.getArgument("name")
                ?: throw IllegalArgumentException("@$JAVA_TYPE_DIRECTIVE_NAME directive must contain 'name' argument")
        return (nameArgument.value as StringValue).value
    }

    private inline fun <reified T : NamedNode<*>> index(definitions: Collection<Definition<*>>): Map<String, List<T>> =
        definitions.filterIsInstance<T>().groupBy { it.name }
}
