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

package com.netflix.graphql.dgs.codegen.generators.java

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.fieldDefinitions
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import graphql.language.*
import org.slf4j.LoggerFactory

@Suppress("UNCHECKED_CAST")
class EntitiesRepresentationTypeGenerator(config: CodeGenConfig, document: Document) : BaseDataTypeGenerator(config.packageNameClient, config, document) {

    fun generate(definition: ObjectTypeDefinition, generatedRepresentations: MutableMap<String, Any>): CodeGenResult {
        if (config.skipEntityQueries) {
            return CodeGenResult()
        }
        val representationName = toRepresentationName(definition)
        if (generatedRepresentations.containsKey(representationName)) {
            return CodeGenResult()
        }
        val directiveArg =
            definition
                .getDirectives("key")
                .map { it.argumentsByName["fields"]?.value as StringValue }
                .map { it.value }

        val keyFields = parseKeyDirectiveValue(directiveArg)
        return generateRepresentations(
            definition.name,
            representationName,
            definition.fieldDefinitions,
            generatedRepresentations,
            keyFields
        )
    }

    private fun generateRepresentations(
        definitionName: String,
        representationName: String,
        fields: List<FieldDefinition>,
        generatedRepresentations: MutableMap<String, Any>,
        keyFields: Map<String, Any>
    ): CodeGenResult {
        if (generatedRepresentations.containsKey(representationName)) {
            return CodeGenResult()
        }
        var fieldsCodeGenAccumulator = CodeGenResult()
        // generate representations of entity types that have @key, including the __typename field, and the  key fields
        val typeName = Field("__typename", ClassName.get(String::class.java), CodeBlock.of("\$S", definitionName))
        val fieldDefinitions =
            fields
                .filter { keyFields.containsKey(it.name) }
                .map {
                    val type = findType(it.type, document)

                    if (type != null &&
                        (
                            type is ObjectTypeDefinition ||
                                type is InterfaceTypeDefinition ||
                                type is EnumTypeDefinition
                            )
                    ) {
                        val fieldTypeRepresentationName = toRepresentationName(type)
                        val fieldRepresentationType =
                            typeUtils
                                .findReturnType(it.type)
                                .toString()
                                .replace(type.name, fieldTypeRepresentationName)

                        if (generatedRepresentations.containsKey(fieldTypeRepresentationName)) {
                            logger.trace("Representation fo $fieldTypeRepresentationName was already generated.")
                        } else {
                            logger.debug("Generating entity representation {} ...", fieldTypeRepresentationName)
                            val fieldTypeRepresentation = generateRepresentations(
                                type.name,
                                fieldTypeRepresentationName,
                                type.fieldDefinitions(),
                                generatedRepresentations,
                                keyFields[it.name] as Map<String, Any>
                            )
                            fieldsCodeGenAccumulator = fieldsCodeGenAccumulator.merge(fieldTypeRepresentation)
                            generatedRepresentations[fieldTypeRepresentationName] = fieldRepresentationType
                        }
                        Field(it.name, ClassName.get("", fieldRepresentationType))
                    } else {
                        val returnType = typeUtils.findReturnType(it.type)
                        Field(it.name, returnType)
                    }
                }
        // Generate base type representation...
        val parentRepresentationCodeGen = super.generate(
            name = representationName,
            interfaces = emptyList(),
            fields = fieldDefinitions.plus(typeName),
            description = null
        )
        generatedRepresentations[representationName] = typeUtils.qualifyName(representationName)
        // Merge all results.
        return parentRepresentationCodeGen.merge(fieldsCodeGenAccumulator)
    }

    private fun findType(typeName: Type<*>, document: Document): TypeDefinition<*>? {
        return when (typeName) {
            is NonNullType -> {
                findType(typeName.type, document)
            }
            is ListType -> {
                findType(typeName.type, document)
            }
            else -> document.definitions.filterIsInstance<TypeDefinition<*>>()
                .find { it.name == (typeName as TypeName).name }
        }
    }

    private fun parseKeyDirectiveValue(keyDirective: List<String>): Map<String, Any> {
        data class Node(val key: String, val map: MutableMap<String, Any>, val parent: Node?)

        val keys = keyDirective.map { ds ->
            ds.map { if (it == '{' || it == '}') " $it " else "$it" }
                .joinToString("", "", "")
                .split(" ")
        }.flatten()

        // handle simple keys and nested keys by constructing the path to each  key
        // e.g. type Movie @key(fields: "movieId") or type MovieCast @key(fields: movie { movieId } actors { name } }
        val mappedKeyTypes = mutableMapOf<String, Any>()
        var parent = Node("", mappedKeyTypes, null)
        var current = Node("", mappedKeyTypes, null)
        keys.filter { it != " " && it != "" }
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

    companion object {
        private val logger: org.slf4j.Logger =
            LoggerFactory.getLogger(EntitiesRepresentationTypeGenerator::class.java)

        private fun toRepresentationName(definition: TypeDefinition<*>) = "${definition.name}Representation"
    }
}
