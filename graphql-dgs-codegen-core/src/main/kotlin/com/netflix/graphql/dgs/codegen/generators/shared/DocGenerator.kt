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

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.findTypeDefinition
import graphql.language.*
import graphql.parser.Parser
import graphql.schema.idl.TypeUtil
import kotlinx.serialization.json.*
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.abs
import kotlin.random.Random

@Suppress("FoldInitializerAndIfToElvis")
class DocGenerator(private val config: CodeGenConfig, private val document: Document) {
    private val gqlParser = Parser()
    private val packageName = config.packageNameDocs
    fun generate(
        definition: Definition<*>
    ): CodeGenResult {
        if (definition !is ObjectTypeDefinition) {
            return CodeGenResult()
        }

        val docFiles: MutableList<DocFileSpec> = mutableListOf()

        if (definition.name.equals("Query")) {
            definition.fieldDefinitions.forEach {
                val markdownText: String? = getFieldDefinitionMarkdown(it, definition)
                if (markdownText != null) {
                    docFiles.add(DocFileSpec.get("${definition.name}.${it.name}", markdownText))
                }
            }
        }

        if (definition.directivesByName["key"] != null) {
            val entitiesMarkdown: String? = getEntitiesMarkdown(definition)
            if (entitiesMarkdown != null) {
                docFiles.add(DocFileSpec.get("Entities.${definition.name}", entitiesMarkdown))
            }
        }

        return CodeGenResult(docFiles = docFiles)
    }

    private fun getFieldDefinitionMarkdown(field: FieldDefinition, definition: ObjectTypeDefinition): String? {
        return """
            |# ${definition.name}.${field.name}: ${AstPrinter.printAst(field.type)}
            ${if (field.inputValueDefinitions.size > 0) {
            """     
            |## Arguments
            || Name | Description | Required | Type |
            || :--- | :---------- | :------: | :--: |
            ${field.inputValueDefinitions.map {
                """
            || ${it.name} | ${it.description?.getContent()?.replace("|", "\\|") ?: ""} | ${if (it.type is NonNullType) "✅" else "Optional"} | ${AstPrinter.printAst(it.type)} |
                """.trimIndent()
            }.joinToString("\n")}
            """
        }else ""}
            |## Example
            |```graphql
            |${getExampleQuery(field)?.split("\n")?.joinToString("\n|")}
            |```
        """.trimMargin()
    }

    private fun getEntitiesMarkdown(definition: Definition<*>): String? {
        if (definition is ObjectTypeDefinition) {
            return """
                |# ${definition.name} - Federated Entities Query
                |### Query
                |```graphql
                |${getExampleEntitiesQuery(definition)?.split("\n")?.joinToString("\n|")}
                |```
                |
                |### Variables
                |```json
                |${getExampleEntitiesQueryVariables(definition)?.split("\n")?.joinToString("\n|")}
                |```
            """.trimMargin()
        }

        return null
    }

    private fun getExampleQuery(definition: FieldDefinition): String? {
        val selectionSet: List<String> = getSelectionSet(definition.type.findTypeDefinition(document))
        val gql: String = """
            {
                ${definition.name}${if (definition.inputValueDefinitions.size > 0) "(${definition.inputValueDefinitions.map{ "${it.name}: ${getMockGQLValueAsAST(it.type)}"}.joinToString(", ")})" else ""} ${if (selectionSet.size > 0) "{${selectionSet.joinToString("\n")}}" else ""}
            }
        """.trimIndent()
        return AstPrinter.printAst(gqlParser.parseDocument(gql))
    }

    private fun getExampleEntitiesQuery(definition: ObjectTypeDefinition): String? {
        val gql: String = """
            query(${'$'}representations: [_Any!]!) {
                ... on ${definition.name} {
                    ${definition.fieldDefinitions.map {
            val selectionSet : List<String> = getSelectionSet(it.type.findTypeDefinition(document))
            """
                            ${it.name}${if (it.inputValueDefinitions.size > 0) "(${it.inputValueDefinitions.map{ "${it.name}: ${getMockGQLValueAsAST(it.type)}"}.joinToString(", ")})" else ""} ${if (selectionSet.size > 0) "{${selectionSet.joinToString("\n")}}" else ""}
                        """
        }.joinToString("\n")}
                }
            }
        """.trimIndent()
        return AstPrinter.printAst(gqlParser.parseDocument(gql))
    }

    private fun getExampleEntitiesQueryVariables(definition: ObjectTypeDefinition): String? {
        val representations: MutableList<JsonElement> = mutableListOf()

        val fieldsArgument: Argument? = definition.getDirectives("key")?.get(0)?.getArgument("fields")
        if (fieldsArgument == null) {
            return null
        }

        if (fieldsArgument.value is StringValue) {
            val strValue: StringValue = fieldsArgument.value as StringValue
            val keyFieldsDoc: Document = gqlParser.parseDocument("query { ${strValue.value} }")

            keyFieldsDoc.definitions.filterIsInstance<OperationDefinition>().forEach { operationDefinition ->
                operationDefinition.selectionSet.selections.filterIsInstance<Field>().forEach {
                    val rootElement: JsonObject = getEntitiesSelectionSet(it, definition)
                    representations.add(rootElement)
                }
            }

            val json: Map<String, JsonElement> = mapOf("representations" to JsonArray(representations))
            return JsonObject(json).toString()
        }

        return null
    }

    private fun getEntitiesSelectionSet(field: Field, definition: Node<*>?): JsonObject {
        // Handle sub-selections recursively
        if (field.selectionSet != null) {
            val map = mapOf(
                field.name to JsonArray(
                    field.selectionSet.selections.filterIsInstance<Field>().map {
                        val nextDef: Node<*>? = if (definition is ObjectTypeDefinition) definition.fieldDefinitions.first { it.name.equals(field.name) } as Node<*> else definition
                        getEntitiesSelectionSet(it, nextDef)
                    }
                )
            )
            return JsonObject(map)
        }

        var fieldValue: MutableMap<String, JsonElement>?

        if (definition is ObjectTypeDefinition) {
            val gqlValue: Value<*> = getMockGQLValue(definition.fieldDefinitions.first { it.name.equals(field.name) }?.type)
            val mockedValue: JsonElement = toJsonPrimitive(gqlValue)
            fieldValue = mutableMapOf(field.name to mockedValue)
            fieldValue.put("__typename", JsonPrimitive(definition.name))
        } else {
            fieldValue = mutableMapOf(field.name to JsonPrimitive("foo"))
        }

        return JsonObject(fieldValue)
    }

    private fun getMockGQLValueAsAST(type: Type<*>?): String {
        return AstPrinter.printAst(getMockGQLValue(type))
    }

    private fun getMockGQLValue(type: Type<*>?): Value<*> {
        if (TypeUtil.isWrapped(type)) {
            return getMockGQLValue(TypeUtil.unwrapAll(type))
        }

        if (type is TypeName) {
            return when (type.name) {
                "ID" -> StringValue("random${Random.nextInt()}")
                "String" -> StringValue("randomString")
                "Boolean" -> BooleanValue(Random.nextBoolean())
                "Int" -> IntValue(BigInteger.valueOf(abs(Random.nextLong())))
                "Float" -> FloatValue(BigDecimal.valueOf(abs(Random.nextDouble())))
                else -> {
                    getSchemaTypeMockValue(type)
                }
            }
        }

        return NullValue.of()
    }

    private fun getSchemaTypeMockValue(type: TypeName): Value<*> {
        val typeDef: Definition<*>? = document.definitions?.firstOrNull {
            when (it) {
                is EnumTypeDefinition -> it.name.equals(type.name)
                is InputObjectTypeDefinition -> it.name.equals(type.name)
                else -> false
            }
        }

        if (typeDef == null) {
            return NullValue.of()
        }

        if (typeDef is EnumTypeDefinition) {
            return EnumValue(typeDef.enumValueDefinitions.random().name)
        } else if (typeDef is InputObjectTypeDefinition) {
            return ObjectValue(typeDef.inputValueDefinitions.map { ObjectField(it.name, getMockGQLValue(it.type)) })
        }

        return NullValue.of()
    }

    private fun getSelectionSet(typeDef: TypeDefinition<*>?): List<String> {
        if (typeDef is ObjectTypeDefinition) {
            return typeDef.fieldDefinitions.map { if (it.inputValueDefinitions.size > 0) "${it.name}(${it.inputValueDefinitions.map{ "${it.name}: ${getMockGQLValueAsAST(it.type)}"}.joinToString(", ")})" else it.name }
        } else {
            return listOf()
        }
    }

    private fun toJsonPrimitive(input: Any?): JsonElement {
        if (input is NullValue) {
            return JsonNull
        } else if (input is FloatValue) {
            return JsonPrimitive(input.value)
        } else if (input is StringValue) {
            return JsonPrimitive(input.value)
        } else if (input is IntValue) {
            return JsonPrimitive(input.value)
        } else if (input is BooleanValue) {
            return JsonPrimitive(input.isValue)
        } else if (input is EnumValue) {
            return JsonPrimitive(input.name)
        } else if (input is ArrayValue) {
            val elements: List<JsonElement> = input.values.map { toJsonPrimitive(it) }
            return JsonArray(elements)
        } else if (input is ObjectValue) {
            return JsonObject(input.objectFields.map { it.name to toJsonPrimitive(it.value) }.toMap())
        }

        return JsonNull
    }
}
