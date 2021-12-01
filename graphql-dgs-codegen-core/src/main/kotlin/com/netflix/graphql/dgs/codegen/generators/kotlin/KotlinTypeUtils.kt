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
import com.netflix.graphql.dgs.codegen.generators.shared.parseMappedType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import graphql.language.*
import graphql.language.TypeName
import graphql.parser.Parser
import graphql.relay.PageInfo
import graphql.util.TraversalControl
import graphql.util.TraverserContext
import java.time.*
import java.util.*
import com.squareup.kotlinpoet.TypeName as KtTypeName

class KotlinTypeUtils(private val packageName: String, val config: CodeGenConfig) {

    private val commonScalars = mapOf(
        "LocalTime" to LocalTime::class.asTypeName(),
        "LocalDate" to LocalDate::class.asTypeName(),
        "LocalDateTime" to LocalDateTime::class.asTypeName(),
        "TimeZone" to STRING,
        "Currency" to Currency::class.asTypeName(),
        "Instant" to Instant::class.asTypeName(),
        "Date" to LocalDate::class.asTypeName(),
        "DateTime" to OffsetDateTime::class.asTypeName(),
        "RelayPageInfo" to PageInfo::class.asTypeName(),
        "PageInfo" to PageInfo::class.asTypeName(),
        "PresignedUrlResponse" to "com.netflix.graphql.types.core.resolvers.PresignedUrlResponse".toKtTypeName(),
        "Header" to "com.netflix.graphql.types.core.resolvers.PresignedUrlResponse.Header".toKtTypeName(),
    )

    fun findReturnType(fieldType: Type<*>): KtTypeName {
        val visitor = object : NodeVisitorStub() {
            override fun visitTypeName(node: TypeName, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                context.setAccumulate(node.toKtTypeName().copy(nullable = true))
                return TraversalControl.CONTINUE
            }
            override fun visitListType(node: ListType, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                val typeName = context.getCurrentAccumulate<KtTypeName>()
                context.setAccumulate(LIST.parameterizedBy(typeName).copy(nullable = true))
                return TraversalControl.CONTINUE
            }
            override fun visitNonNullType(node: NonNullType, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                val typeName = context.getCurrentAccumulate<KtTypeName>()
                context.setAccumulate(typeName.copy(nullable = false))
                return TraversalControl.CONTINUE
            }
            override fun visitNode(node: Node<*>, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                throw AssertionError("Unknown field type: $node")
            }
        }
        return NodeTraverser().postOrder(visitor, fieldType) as KtTypeName
    }

    fun isNullable(fieldType: Type<*>): Boolean {
        return if (config.kotlinAllFieldsOptional)
            true
        else
            fieldType !is NonNullType
    }

    private fun TypeName.toKtTypeName(): KtTypeName {
        if (name in config.typeMapping) {
            val mappedType = config.typeMapping.getValue(name)

            return parseMappedType(
                mappedType = mappedType,
                toTypeName = String::toKtTypeName,
                parameterize = { (it.first as ClassName).parameterizedBy(it.second) },
                onCloseBracketCallBack = { current, typeString ->
                    if (typeString.trim() == "?") {
                        val last = current.second.removeLast()
                        current.second.add(last.copy(nullable = true))
                    } else {
                        current.second.add(typeString.toKtTypeName(true))
                    }
                }
            )
        }

        if (name in config.schemaTypeMapping) {
            return config.schemaTypeMapping.getValue(name).toKtTypeName()
        }

        if (name in commonScalars) {
            return commonScalars.getValue(name)
        }

        return when (name) {
            "String" -> STRING
            "StringValue" -> STRING
            "Int" -> INT
            "IntValue" -> INT
            "Float" -> DOUBLE
            "FloatValue" -> DOUBLE
            "Boolean" -> BOOLEAN
            "BooleanValue" -> BOOLEAN
            "ID" -> STRING
            "IDValue" -> STRING
            else -> "$packageName.$name".toKtTypeName()
        }
    }

    private val CodeGenConfig.schemaTypeMapping: Map<String, String>
        get() {
            val inputSchemas = this.schemaFiles.flatMap { it.walkTopDown().toList().filter { file -> file.isFile } }
                .map { it.readText() }
                .plus(this.schemas)
            val joinedSchema = inputSchemas.joinToString("\n")
            val document = Parser().parseDocument(joinedSchema)

            return document.definitions.filterIsInstance<ScalarTypeDefinition>().filterNot {
                it.getDirectives("javaType").isNullOrEmpty()
            }.associate {
                val javaType = it.getDirectives("javaType").singleOrNull()
                    ?: throw IllegalArgumentException("multiple @javaType directives are defined")
                val value = javaType.argumentsByName["name"]?.value
                    ?: throw IllegalArgumentException("@javaType directive must contains name argument")
                it.name to (value as StringValue).value
            }
        }
}
