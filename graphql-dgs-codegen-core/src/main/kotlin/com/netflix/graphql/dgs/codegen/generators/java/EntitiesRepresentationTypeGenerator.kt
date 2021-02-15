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
import com.squareup.javapoet.ClassName
import graphql.language.*


@Suppress("UNCHECKED_CAST")
class EntitiesRepresentationTypeGenerator(val config: CodeGenConfig): BaseDataTypeGenerator(config.packageName + ".client", config) {
    fun generate(definition: ObjectTypeDefinition, document: Document, generatedRepresentations: MutableMap<String, Any>): CodeGenResult {
        if(config.skipEntityQueries) {
            return CodeGenResult()
        }

        val name = "${definition.name}Representation"
        if (generatedRepresentations.containsKey(name)) {
            return CodeGenResult()
        }
        val directiveArg = (definition.getDirective("key").argumentsByName["fields"]?.value as StringValue).value
        val keyFields = parseKeyDirectiveValue(directiveArg)
        return generateRepresentations(definition, document, generatedRepresentations, keyFields)
    }

    private fun generateRepresentations(definition: ObjectTypeDefinition, document: Document, generatedRepresentations: MutableMap<String, Any>,
                                        keyFields: Map<String, Any>): CodeGenResult {
        val name = "${definition.name}Representation"
        if (generatedRepresentations.containsKey(name)) {
            return CodeGenResult()
        }
        var result = CodeGenResult()
        // generate representations of entity types that have @key, including the __typename field, and the  key fields
        val typeName = Field("__typename", ClassName.get(String::class.java), definition.name)
        val fieldDefinitions = definition.fieldDefinitions
                .filter {
                    keyFields.containsKey(it.name)
                }
                .map {
                    val type = findType(it.type, document)
                    if (type != null && type is ObjectTypeDefinition) {
                        val representationType = typeUtils.findReturnType(it.type).toString().replace(type.name, "${type.name}Representation")
                        if (! generatedRepresentations.containsKey(name)) {
                            result = generateRepresentations(type, document, generatedRepresentations, keyFields[it.name] as Map<String, Any>)
                        }
                        generatedRepresentations["${type.name}Representation"] = representationType
                        Field(it.name, ClassName.get("", representationType))
                    } else {
                        Field(it.name, typeUtils.findReturnType(it.type))
                    }
                }
        return generate(name, emptyList(), fieldDefinitions.plus(typeName), true, false).merge(result)
    }

    private fun findType(typeName: Type<*>, document: Document): TypeDefinition<*>? {
        return when (typeName) {
            is NonNullType -> {
                findType(typeName.type, document)
            }
            is ListType -> {
                findType(typeName.type, document)
            }
            else -> document.definitions.filterIsInstance<TypeDefinition<*>>().find { it.name == (typeName as TypeName).name }
        }
    }

    private fun parseKeyDirectiveValue(keyDirective: String): Map<String, Any> {
        data class Node (val key: String, val map: MutableMap<String, Any>, val parent: Node?)
        val sanitizedKeys =  keyDirective.map { if (it == '{' || it == '}') " $it "  else "$it" }
        val keys = sanitizedKeys.joinToString("", "", "").split(" ")

        // handle simple keys and nested keys by constructing the path to each  key
        // e.g. type Movie @key(fields: "movieId") or type MovieCast @key(fields: movie { movieId } actors { name } }
        val mappedKeyTypes = mutableMapOf<String, Any>()
        var parent =  Node("", mappedKeyTypes, null)
        var current =  Node("", mappedKeyTypes, null)
        keys.filter { it  !=  " " && it != "" }
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
}
