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
import com.squareup.javapoet.TypeName as JavaTypeName
import graphql.language.ListType
import graphql.language.Node
import graphql.language.NodeTraverser
import graphql.language.NodeVisitorStub
import graphql.language.NonNullType
import graphql.language.Type
import graphql.language.TypeName
import graphql.relay.PageInfo
import graphql.util.TraversalControl
import graphql.util.TraverserContext

import java.time.*
import java.util.*

class TypeUtils(private val packageName: String, private val config: CodeGenConfig) {
    private val commonScalars =  mutableMapOf<String, com.squareup.javapoet.TypeName>(
        "LocalTime" to ClassName.get(LocalTime::class.java),
        "LocalDate" to ClassName.get(LocalDate::class.java),
        "LocalDateTime" to ClassName.get(LocalDateTime::class.java),
        "TimeZone" to ClassName.get(String::class.java),
        "DateTime" to ClassName.get(OffsetDateTime::class.java),
        "Currency" to ClassName.get(Currency::class.java),
        "Instant" to ClassName.get(Instant::class.java),
        "RelayPageInfo" to ClassName.get(PageInfo::class.java),
        "PageInfo" to ClassName.get(PageInfo::class.java),
        "PresignedUrlResponse" to ClassName.get("com.netflix.graphql.types.core.resolvers", "PresignedUrlResponse"),
        "Header" to ClassName.get("com.netflix.graphql.types.core.resolvers", "PresignedUrlResponse", "Header"))

    fun findReturnType(fieldType: Type<*>, useInterfaceType: Boolean = false): JavaTypeName {
        val visitor = object : NodeVisitorStub() {
            override fun visitTypeName(node: TypeName, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                val typeName = node.toJavaTypeName(useInterfaceType)
                val boxed = boxType(typeName)
                context.setAccumulate(boxed)
                return TraversalControl.CONTINUE
            }
            override fun visitListType(node: ListType, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                val typeName = context.getCurrentAccumulate<JavaTypeName>()
                val boxed = boxType(typeName)
                val parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(List::class.java), boxed)
                context.setAccumulate(parameterizedTypeName)
                return TraversalControl.CONTINUE
            }
            override fun visitNonNullType(node: NonNullType, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                val typeName = context.getCurrentAccumulate<JavaTypeName>()
                val accumulate = if (config.generateBoxedTypes) {
                    boxType(typeName)
                } else {
                    unboxType(typeName)
                }
                context.setAccumulate(accumulate)
                return TraversalControl.CONTINUE
            }
            override fun visitNode(node: Node<*>, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                throw AssertionError("Unknown field type: $node")
            }
        }
        return NodeTraverser().postOrder(visitor, fieldType) as JavaTypeName
    }

    private fun unboxType(typeName: JavaTypeName): JavaTypeName {
        return if (typeName.isBoxedPrimitive) {
            typeName.unbox()
        } else {
            typeName
        }
    }

    private fun boxType(typeName: JavaTypeName): JavaTypeName {
        return if (typeName.isPrimitive) {
            typeName.box()
        } else {
            typeName
        }
    }

    private fun TypeName.toJavaTypeName(useInterfaceType: Boolean): JavaTypeName {
        if (name in config.typeMapping) {
            println("Found mapping for type: $name")
            val mappedType = config.typeMapping.getValue(name)
            return ClassName.bestGuess(mappedType)
        }

        if(commonScalars.containsKey(name)) {
            return commonScalars[name]!!
        }

        return when (name) {
            "String" -> ClassName.get(String::class.java)
            "StringValue" -> ClassName.get(String::class.java)
            "Int" -> JavaTypeName.INT
            "IntValue" -> JavaTypeName.INT
            "Float" -> JavaTypeName.DOUBLE
            "FloatValue" -> JavaTypeName.DOUBLE
            "Boolean" -> JavaTypeName.BOOLEAN
            "BooleanValue" -> JavaTypeName.BOOLEAN
            "ID" -> ClassName.get(String::class.java)
            "IDValue" -> ClassName.get(String::class.java)
            else -> {
                var simpleName = if (useInterfaceType) "I${name}" else name
                ClassName.get(packageName, simpleName)
            }
        }
    }

    fun isStringInput(name: com.squareup.javapoet.TypeName): Boolean {
        if (config.typeMapping.containsValue(name.toString())) {
            return when(name) {
                JavaTypeName.INT -> false
                JavaTypeName.DOUBLE -> false
                JavaTypeName.BOOLEAN -> false
                JavaTypeName.INT.box() -> false
                JavaTypeName.DOUBLE.box() -> false
                JavaTypeName.BOOLEAN.box() -> false
                else -> true
            }
        }
        return  (name == ClassName.get(String::class.java) || commonScalars.containsValue(name))
    }
}
