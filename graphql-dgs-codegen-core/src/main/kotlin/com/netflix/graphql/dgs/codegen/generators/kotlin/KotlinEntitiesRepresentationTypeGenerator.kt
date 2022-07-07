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

package com.netflix.graphql.dgs.codegen.generators.kotlin

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.fieldDefinitions
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import graphql.language.Document
import graphql.language.FieldDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.StringValue
import graphql.language.Type
import graphql.language.TypeDefinition
import graphql.language.TypeName

class KotlinEntitiesRepresentationTypeGenerator(config: CodeGenConfig, document: Document) :
    AbstractKotlinDataTypeGenerator(packageName = config.packageNameClient, config = config, document = document) {

    fun generate(definition: ObjectTypeDefinition, generatedRepresentations: MutableMap<String, Any>): CodeGenResult {
        val name = "${definition.name}Representation"
        if (name in generatedRepresentations) {
            return CodeGenResult()
        }
        val directiveArg = definition.getDirectives("key")
            .asSequence()
            .map { it.argumentsByName["fields"]?.value as StringValue }
            .map { it.value }
            .toList()
        val keyFields = parseKeyDirectiveValue(directiveArg)
        return generateRepresentations(definition.name, definition.fieldDefinitions, generatedRepresentations, keyFields)
    }

    private fun generateRepresentations(
        definitionName: String,
        fields: List<FieldDefinition>,
        generatedRepresentations: MutableMap<String, Any>,
        keyFields: Map<String, Any>
    ): CodeGenResult {
        val name = "${definitionName}Representation"
        if (name in generatedRepresentations) {
            return CodeGenResult()
        }

        var result = CodeGenResult()
        // generate representations of entity types that have @key, including the __typename field, and the  key fields
        val typeName = Field("__typename", STRING, false, CodeBlock.of("%S", definitionName))
        val fieldDefinitions = fields
            .filter {
                keyFields.containsKey(it.name)
            }
            .map {
                val type = findType(it.type, document)
                val fieldType = typeUtils.findReturnType(it.type)
                if (type != null && (type is ObjectTypeDefinition || type is InterfaceTypeDefinition)) {
                    val representationType = fieldType.toString().replace(type.name, "${type.name}Representation").removeSuffix("?")
                    if (!generatedRepresentations.containsKey(name)) {
                        result = generateRepresentations(type.name, type.fieldDefinitions(), generatedRepresentations, keyFields[it.name] as Map<String, Any>)
                    }
                    generatedRepresentations["${type.name}Representation"] = representationType
                    if (fieldType is ParameterizedTypeName && fieldType.rawType.simpleName == "List") {
                        Field(it.name, LIST.parameterizedBy(ClassName(getPackageName(), "${type.name}Representation")), typeUtils.isNullable(it.type))
                    } else {
                        Field(it.name, ClassName(getPackageName(), representationType), typeUtils.isNullable(it.type))
                    }
                } else {
                    Field(it.name, typeUtils.findReturnType(it.type), typeUtils.isNullable(it.type))
                }
            }

        return generate(name, fieldDefinitions + typeName, emptyList(), document).merge(result)
    }

    override fun getPackageName(): String {
        return config.packageNameClient
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
                        // set the key and corresponding map for the next level
                        val previous = parent
                        parent = current
                        if (current.map.containsKey(current.key)) {
                            current = Node("", current.map[current.key] as MutableMap<String, Any>, previous)
                        }
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
