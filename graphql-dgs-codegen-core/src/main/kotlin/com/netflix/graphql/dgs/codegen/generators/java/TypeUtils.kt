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
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import graphql.language.*
import graphql.relay.PageInfo
import graphql.util.TraversalControl
import graphql.util.TraverserContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.*

class TypeUtils(private val packageName: String, private val config: CodeGenConfig) {
    fun findReturnType(fieldType: Type<*>): com.squareup.javapoet.TypeName {
        val visitor = object : NodeVisitorStub() {
            override fun visitTypeName(node: TypeName, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                val typeName = node.toJavaTypeName()
                val boxed = boxType(typeName)
                context.setAccumulate(boxed)
                return TraversalControl.CONTINUE
            }
            override fun visitListType(node: ListType, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                val typeName = context.getCurrentAccumulate<com.squareup.javapoet.TypeName>()
                val boxed = boxType(typeName)
                val parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(List::class.java), boxed)
                context.setAccumulate(parameterizedTypeName)
                return TraversalControl.CONTINUE
            }
            override fun visitNonNullType(node: NonNullType, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                val typeName = context.getCurrentAccumulate<com.squareup.javapoet.TypeName>()
                val unboxed = unboxType(typeName)
                context.setAccumulate(unboxed)
                return TraversalControl.CONTINUE
            }
            override fun visitNode(node: Node<*>, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                throw AssertionError("Unknown field type: $node")
            }
        }
        return NodeTraverser().postOrder(visitor, fieldType) as com.squareup.javapoet.TypeName
    }

    private fun unboxType(typeName: com.squareup.javapoet.TypeName?): com.squareup.javapoet.TypeName? {
        return when (typeName) {
            com.squareup.javapoet.TypeName.INT.box() -> com.squareup.javapoet.TypeName.INT
            com.squareup.javapoet.TypeName.DOUBLE.box() -> com.squareup.javapoet.TypeName.DOUBLE
            com.squareup.javapoet.TypeName.BOOLEAN.box() -> com.squareup.javapoet.TypeName.BOOLEAN
            else -> typeName
        }
    }

    private fun boxType(typeName: com.squareup.javapoet.TypeName): com.squareup.javapoet.TypeName? {
        return when (typeName) {
            com.squareup.javapoet.TypeName.INT -> com.squareup.javapoet.TypeName.INT.box()
            com.squareup.javapoet.TypeName.DOUBLE -> com.squareup.javapoet.TypeName.DOUBLE.box()
            com.squareup.javapoet.TypeName.BOOLEAN -> com.squareup.javapoet.TypeName.BOOLEAN.box()
            else -> typeName
        }
    }

    private fun TypeName.toJavaTypeName(): com.squareup.javapoet.TypeName {
        if(config.typeMapping.containsKey(name)) {
            println("Found mapping for type: $name")
            val mappedType = config.typeMapping[name]
            return ClassName.get(mappedType?.substringBeforeLast("."), mappedType?.substringAfterLast("."))
        }

        return when (name) {
            "String" -> ClassName.get(String::class.java)
            "StringValue" -> ClassName.get(String::class.java)
            "Int" -> com.squareup.javapoet.TypeName.INT
            "IntValue" -> com.squareup.javapoet.TypeName.INT
            "Float" -> com.squareup.javapoet.TypeName.DOUBLE
            "FloatValue" -> com.squareup.javapoet.TypeName.DOUBLE
            "Boolean" -> com.squareup.javapoet.TypeName.BOOLEAN
            "BooleanValue" -> com.squareup.javapoet.TypeName.BOOLEAN
            "ID" -> ClassName.get(String::class.java)
            "IDValue" -> ClassName.get(String::class.java)
            "LocalTime" -> ClassName.get(LocalTime::class.java)
            "LocalDate" -> ClassName.get(LocalDate::class.java)
            "LocalDateTime" -> ClassName.get(LocalDateTime::class.java)
            "TimeZone" -> ClassName.get(String::class.java)
            "DateTime" -> ClassName.get(OffsetDateTime::class.java)
            "Currency" -> ClassName.get(Currency::class.java)
            "RelayPageInfo" -> ClassName.get(PageInfo::class.java)
            "PageInfo" -> ClassName.get(PageInfo::class.java)
            "PresignedUrlResponse" -> ClassName.get("com.netflix.graphql.types.core.resolvers", "PresignedUrlResponse")
            "Header" -> ClassName.get("", "com.netflix.graphql.types.core.resolvers.PresignedUrlResponse.Header")
            else -> ClassName.get(packageName, name)
        }
    }
}