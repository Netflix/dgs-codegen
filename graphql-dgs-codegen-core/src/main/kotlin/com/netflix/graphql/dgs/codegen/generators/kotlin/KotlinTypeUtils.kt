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
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.asTypeName
import graphql.language.ListType
import graphql.language.Node
import graphql.language.NodeTraverser
import graphql.language.NodeVisitorStub
import graphql.language.NonNullType
import graphql.language.ScalarTypeDefinition
import graphql.language.StringValue
import graphql.language.Type
import graphql.language.TypeName
import graphql.parser.Parser
import graphql.relay.PageInfo
import graphql.util.TraversalControl
import graphql.util.TraverserContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.Currency
import com.squareup.kotlinpoet.TypeName as KtTypeName

class KotlinTypeUtils(private val packageName: String, val config: CodeGenConfig) {

    private val commonScalars = mapOf<String, KtTypeName>(
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
        "PresignedUrlResponse" to ClassName.bestGuess("com.netflix.graphql.types.core.resolvers.PresignedUrlResponse"),
        "Header" to ClassName.bestGuess("com.netflix.graphql.types.core.resolvers.PresignedUrlResponse.Header")
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
            return ClassName.bestGuess(config.typeMapping.getValue(name))
        }

        if (name in config.schemaTypeMapping) {
            return ClassName.bestGuess(config.schemaTypeMapping.getValue(name))
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
            else -> ClassName.bestGuess("$packageName.$name")
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
