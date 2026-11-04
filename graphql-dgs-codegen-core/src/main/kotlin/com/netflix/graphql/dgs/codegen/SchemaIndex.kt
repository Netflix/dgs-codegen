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
import graphql.language.EnumTypeDefinition
import graphql.language.EnumTypeExtensionDefinition
import graphql.language.InputObjectTypeExtensionDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.InterfaceTypeExtensionDefinition
import graphql.language.NamedNode
import graphql.language.ObjectTypeDefinition
import graphql.language.ObjectTypeExtensionDefinition
import graphql.language.OperationDefinition.Operation
import graphql.language.ScalarTypeDefinition
import graphql.language.SchemaDefinition
import graphql.language.SchemaExtensionDefinition
import graphql.language.StringValue
import graphql.language.TypeDefinition
import graphql.language.UnionTypeDefinition
import graphql.language.UnionTypeExtensionDefinition
import java.util.Locale

/** Immutable, document-order-preserving indexes over a parsed schema. */
internal class SchemaIndex(
    internal val document: Document,
) {
    internal val definitions: List<Definition<*>> = document.definitions

    internal val operationRoots: Map<Operation, String> =
        run {
            val schemas = definitions.filterIsInstance<SchemaDefinition>()
            val roots = linkedMapOf<Operation, String>()
            // Mutation and subscription defaults require an implicit schema definition.
            if (schemas.none { it !is SchemaExtensionDefinition }) {
                roots[Operation.QUERY] = "Query"
                roots[Operation.MUTATION] = "Mutation"
                roots[Operation.SUBSCRIPTION] = "Subscription"
            }
            for (schema in schemas) {
                for (operation in schema.operationTypeDefinitions) {
                    roots[Operation.valueOf(operation.name.uppercase(Locale.ROOT))] = operation.typeName.name
                }
            }
            roots.putIfAbsent(Operation.QUERY, "Query")
            roots.toSortedMap(compareBy { it.ordinal })
        }

    // A single object type can serve more than one operation.
    internal val operationTypes: Map<String, Set<Operation>> =
        operationRoots.entries.groupBy({ it.value }, { it.key }).mapValues { it.value.toSet() }

    /** Operation roots also selected through fields, unions or interface fragments. */
    internal val nestedOperationTypes: Set<String> by lazy {
        buildSet {
            for (definition in definitions.filterIsInstance<TypeDefinition<*>>()) {
                definition.fieldDefinitions().mapNotNullTo(this) { it.type.findTypeDefinition(this@SchemaIndex)?.name }
                if (definition is UnionTypeDefinition) {
                    definition.memberTypes.filterIsInstance<NamedNode<*>>().mapTo(this) { it.name }
                }
                if (definition is ObjectTypeDefinition && definition.implements.isNotEmpty()) {
                    add(definition.name)
                }
            }
        }.intersect(operationTypes.keys)
    }

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

    fun hasObjectType(name: String): Boolean = definitions(name).any { it is ObjectTypeDefinition }

    fun hasInterfaceType(name: String): Boolean = definitions(name).any { it is InterfaceTypeDefinition }

    fun hasEnumType(name: String): Boolean = definitions(name).any { it is EnumTypeDefinition }

    fun hasUnionType(name: String): Boolean = definitions(name).any { it is UnionTypeDefinition }

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
