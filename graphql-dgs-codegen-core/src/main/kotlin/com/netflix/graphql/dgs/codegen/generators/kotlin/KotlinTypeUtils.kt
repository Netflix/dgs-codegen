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
import com.netflix.graphql.dgs.codegen.generators.shared.findSchemaTypeMapping
import com.netflix.graphql.dgs.codegen.generators.shared.parseMappedType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import graphql.language.*
import graphql.language.TypeName
import graphql.relay.PageInfo
import graphql.util.TraversalControl
import graphql.util.TraverserContext
import java.time.*
import java.util.*
import com.squareup.kotlinpoet.TypeName as KtTypeName

class KotlinTypeUtils(
    private val packageName: String,
    private val config: CodeGenConfig,
    private val document: Document,
) {
    private val commonScalars =
        mapOf(
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
            "Upload" to "org.springframework.web.multipart.MultipartFile".toKtTypeName(),
        )

    fun qualifyName(name: String): String = "$packageName.$name"

    fun findReturnType(fieldType: Type<*>): KtTypeName {
        val visitor =
            object : NodeVisitorStub() {
                override fun visitTypeName(
                    node: TypeName,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl {
                    context.setAccumulate(node.toKtTypeName().copy(nullable = true))
                    return TraversalControl.CONTINUE
                }

                override fun visitListType(
                    node: ListType,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl {
                    val typeName = context.getCurrentAccumulate<KtTypeName>()
                    context.setAccumulate(LIST.parameterizedBy(typeName).copy(nullable = true))
                    return TraversalControl.CONTINUE
                }

                override fun visitNonNullType(
                    node: NonNullType,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl {
                    val typeName = context.getCurrentAccumulate<KtTypeName>()
                    context.setAccumulate(typeName.copy(nullable = false))
                    return TraversalControl.CONTINUE
                }

                override fun visitNode(
                    node: Node<*>,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl = throw AssertionError("Unknown field type: $node")
            }
        return NodeTraverser().postOrder(visitor, fieldType) as KtTypeName
    }

    fun isNullable(fieldType: Type<*>): Boolean =
        if (config.kotlinAllFieldsOptional) {
            true
        } else {
            fieldType !is NonNullType
        }

    /**
     * Takes a GQL interface type name and returns the appropriate kotlin type given all of the mappings defined in the schema and config
     */
    fun findKtInterfaceName(
        interfaceName: String,
        packageName: String,
    ): KtTypeName {
        // check config
        if (interfaceName in config.typeMapping) {
            val mappedType = config.typeMapping.getValue(interfaceName)

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
                },
            )
        }

        return "$packageName.$interfaceName".toKtTypeName()
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
                },
            )
        }

        val schemaType = findSchemaTypeMapping(document, name)
        if (schemaType != null) {
            return schemaType.toKtTypeName()
        }

        if (name in commonScalars && !isFieldTypeDefinedInDocument(name)) {
            return commonScalars.getValue(name)
        }

        val typeName = config.typePrefix + name + config.typeSuffix

        return when (name) {
            STRING.simpleName -> STRING
            "StringValue" -> STRING
            INT.simpleName -> INT
            "IntValue" -> INT
            FLOAT.simpleName -> DOUBLE
            "FloatValue" -> DOUBLE
            BOOLEAN.simpleName -> BOOLEAN
            "BooleanValue" -> BOOLEAN
            "ID" -> STRING
            "IDValue" -> STRING
            else -> "${config.packageNameTypes}.$typeName".toKtTypeName()
        }
    }

    private fun isFieldTypeDefinedInDocument(name: String): Boolean =
        document.definitions.filterIsInstance<ObjectTypeDefinition>().any { e -> e.name == name } ||
            document.definitions.filterIsInstance<EnumTypeDefinition>().any { e -> e.name == name }
}
