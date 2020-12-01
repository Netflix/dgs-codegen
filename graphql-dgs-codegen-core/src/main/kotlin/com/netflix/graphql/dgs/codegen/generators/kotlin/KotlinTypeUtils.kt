package com.netflix.graphql.dgs.codegen.generators.kotlin

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName as KtTypeName
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.typeNameOf
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime

class KotlinTypeUtils(private val packageName: String, val config: CodeGenConfig) {
    @ExperimentalStdlibApi
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
        return fieldType !is NonNullType
    }

    @ExperimentalStdlibApi
    private fun TypeName.toKtTypeName(): KtTypeName {
        if (name in config.typeMapping) {
            return ClassName.bestGuess(config.typeMapping.getValue(name))
        }

        return when (name) {
            "String" -> STRING
            "Int" -> INT
            "Float" -> DOUBLE
            "Boolean" -> BOOLEAN
            "ID" -> STRING
            "LocalTime" -> typeNameOf<LocalTime>()
            "LocalDate" -> typeNameOf<LocalDate>()
            "LocalDateTime" -> typeNameOf<LocalDateTime>()
            "TimeZone" -> STRING
            "DateTime" -> typeNameOf<OffsetDateTime>()
            "RelayPageInfo" -> typeNameOf<PageInfo>()
            "PageInfo" -> typeNameOf<PageInfo>()
            else ->  ClassName.bestGuess("${packageName}.${name}")
        }
    }
}