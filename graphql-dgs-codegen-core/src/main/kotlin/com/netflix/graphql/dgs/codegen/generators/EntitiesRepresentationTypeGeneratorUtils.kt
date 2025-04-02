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

package com.netflix.graphql.dgs.codegen.generators

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import graphql.language.*

object EntitiesRepresentationTypeGeneratorUtils {
    fun interface RepresentationGenerator {
        fun generate(
            definitionName: String,
            representationName: String,
            fields: List<FieldDefinition>,
            generatedRepresentations: MutableMap<String, Any>,
            keyFields: Map<String, Any>,
        ): CodeGenResult
    }

    fun generate(
        config: CodeGenConfig,
        definition: ObjectTypeDefinition,
        generatedRepresentations: MutableMap<String, Any>,
        representationGenerator: RepresentationGenerator,
    ): CodeGenResult {
        if (config.skipEntityQueries) {
            return CodeGenResult.EMPTY
        }
        val representationName = toRepresentationName(definition)
        if (representationName in generatedRepresentations) {
            return CodeGenResult.EMPTY
        }

        val directiveArg =
            definition
                .getDirectives("key")
                .map { it.argumentsByName["fields"]?.value as StringValue }
                .map { it.value }

        val keyFields = parseKeyDirectiveValue(directiveArg)

        return representationGenerator.generate(
            definition.name,
            representationName,
            definition.fieldDefinitions,
            generatedRepresentations,
            keyFields,
        )
    }

    fun findType(
        typeName: Type<*>,
        document: Document,
    ): TypeDefinition<*>? =
        when (typeName) {
            is NonNullType -> {
                findType(typeName.type, document)
            }
            is ListType -> {
                findType(typeName.type, document)
            }
            else ->
                document.definitions
                    .filterIsInstance<TypeDefinition<*>>()
                    .find { it.name == (typeName as TypeName).name }
        }

    fun parseKeyDirectiveValue(keyDirective: List<String>): Map<String, Any> {
        data class Node(
            val key: String,
            val map: MutableMap<String, Any>,
            val parent: Node?,
        )

        val keys =
            keyDirective
                .map { ds ->
                    ds
                        .map { if (it == '{' || it == '}') " $it " else "$it" }
                        .joinToString("", "", "")
                        .split(" ")
                }.flatten()

        // handle simple keys and nested keys by constructing the path to each  key
        // e.g. type Movie @key(fields: "movieId") or type MovieCast @key(fields: movie { movieId } actors { name } }
        val mappedKeyTypes = mutableMapOf<String, Any>()
        var parent = Node("", mappedKeyTypes, null)
        var current = Node("", mappedKeyTypes, null)
        keys
            .filter { it != " " && it != "" }
            .forEach {
                when (it) {
                    "{" -> {
                        // push a new map for the next level
                        val previous = parent
                        parent = current
                        current = Node("", current.map[current.key] as MutableMap<String, Any>, previous)
                    }
                    "}" -> {
                        // pop back to parent level
                        current = parent
                        parent = parent.parent!!
                    }
                    else -> {
                        // make an entry at the current level
                        current.map.putIfAbsent(it, mutableMapOf<String, Any>())
                        current = Node(it, current.map, parent)
                    }
                }
            }
        return mappedKeyTypes
    }

    fun toRepresentationName(definition: TypeDefinition<*>) = "${definition.name}Representation"
}
