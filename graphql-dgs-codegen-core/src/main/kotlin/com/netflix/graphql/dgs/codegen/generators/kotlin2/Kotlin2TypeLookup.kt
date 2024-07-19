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

package com.netflix.graphql.dgs.codegen.generators.kotlin2

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.generators.kotlin.toKtTypeName
import com.netflix.graphql.dgs.codegen.generators.shared.JAVA_TYPE_DIRECTIVE_NAME
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findTypeExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.parseMappedType
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import graphql.Scalars
import graphql.language.Document
import graphql.language.EnumTypeDefinition
import graphql.language.ImplementingTypeDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.ListType
import graphql.language.NamedNode
import graphql.language.Node
import graphql.language.NodeTraverser
import graphql.language.NodeVisitorStub
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.OperationDefinition
import graphql.language.ScalarTypeDefinition
import graphql.language.StringValue
import graphql.language.Type
import graphql.language.TypeName
import graphql.language.UnionTypeDefinition
import graphql.util.TraversalControl
import graphql.util.TraverserContext
import com.squareup.kotlinpoet.TypeName as KtTypeName

/**
 * Builds multiple indexes over types defined in the document and config. Used to resolve GQL types to kotlin types during codegen
 */
class Kotlin2TypeLookup(
    config: CodeGenConfig,
    private val document: Document
) {

    /**
     * GQL defined operations
     */
    val operations: Map<String, OperationDefinition.Operation> = mapOf(
        "Query" to OperationDefinition.Operation.QUERY,
        "Mutation" to OperationDefinition.Operation.MUTATION,
        "Subscription" to OperationDefinition.Operation.SUBSCRIPTION
    )

    /**
     * A set of object type names defined in the document
     */
    val objectTypeNames: Set<String> =
        document.getDefinitionsOfType(ObjectTypeDefinition::class.java).map { it.name }.toSet()

    /**
     * A map of GQL builtin scalars to kotlin types
     */
    private val builtinScalars: Map<String, ClassName> = mapOf(
        Scalars.GraphQLInt.name to INT,
        Scalars.GraphQLFloat.name to DOUBLE, // Float: A signed double-precision floating-point value. https://graphql.org/learn/schema/#scalar-types
        Scalars.GraphQLString.name to STRING,
        Scalars.GraphQLBoolean.name to BOOLEAN,
        Scalars.GraphQLID.name to STRING
    )

    /**
     * A map of scalars defined in the document to kotlin types
     */
    private val scalarLookup: Map<String, KtTypeName?> = document
        .getDefinitionsOfType(ScalarTypeDefinition::class.java)
        .associate {

            if (it.hasDirective(JAVA_TYPE_DIRECTIVE_NAME)) {

                val directive = it.getDirectives(JAVA_TYPE_DIRECTIVE_NAME).singleOrNull()
                    ?: throw IllegalArgumentException("multiple @$JAVA_TYPE_DIRECTIVE_NAME directives are defined")
                val nameArgument = directive.getArgument("name")
                    ?: throw IllegalArgumentException("@$JAVA_TYPE_DIRECTIVE_NAME directive must contain 'name' argument")
                val nameValue = nameArgument.value
                if (nameValue !is StringValue) {
                    throw IllegalArgumentException("@$JAVA_TYPE_DIRECTIVE_NAME directive 'name' argument is not a string")
                }

                it.name to nameValue.value.toKtTypeName()
            } else {
                it.name to null
            }
        }

    /**
     * A map of config defined types to kotlin types
     */
    private val mappedTypes: Map<String, KtTypeName> = config
        .typeMapping
        .mapValues { (_, type) ->
            parseMappedType(
                mappedType = type,
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

    /**
     * A map of enum name to list of field names
     */
    private val enumFields: Map<String, List<String>> = document
        .getDefinitionsOfType(EnumTypeDefinition::class.java)
        .associate { i -> i.name to i.enumValueDefinitions.map { it.name } }

    /**
     * A map of interface name to list of field names
     */
    private val interfaceFields: Map<String, List<String>> = document
        .getDefinitionsOfType(InterfaceTypeDefinition::class.java)
        .associate { i -> i.name to i.fieldDefinitions.map { it.name } }

    /**
     * A map of interfaces to the types that implement them
     */
    private val invertedInterfaceLookup: Map<String, List<String>> = document
        .getDefinitionsOfType(InterfaceTypeDefinition::class.java)
        .plus(document.getDefinitionsOfType(ObjectTypeDefinition::class.java))
        .flatMap { o -> o.implements.filterIsInstance<NamedNode<*>>().map { i -> i.name to o.name } }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })

    /**
     * A map from union members to their union type
     */
    private val invertedUnionLookup: Map<String, List<String>> = document
        .getDefinitionsOfType(UnionTypeDefinition::class.java)
        .flatMap { u -> u.memberTypes.filterIsInstance<NamedNode<*>>().map { m -> m.name to u.name } }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })

    /**
     * Returns the types that implement this interface
     */
    fun interfaceImplementors(interfaceName: String): List<String> {
        return invertedInterfaceLookup[interfaceName] ?: emptyList()
    }

    /**
     * Returns the list of interfaces that this type implements
     */
    fun implementedInterfaces(typeDefinition: ImplementingTypeDefinition<*>): List<String> {
        return (
            typeDefinition.implements +
                findTypeExtensions(typeDefinition.name, document.definitions).flatMap { it.implements }
            )
            .filterIsInstance<NamedNode<*>>()
            .filter { it.name != typeDefinition.name }
            .map { it.name }
    }

    /**
     * Returns a list of all unions that contain this type as a member
     */
    fun implementedUnionTypes(typeName: String): List<String> {
        return invertedUnionLookup[typeName] ?: emptyList()
    }

    /**
     * Returns the set of fields that should be overridden
     */
    fun overrideFields(implementedInterfaces: List<String>): Set<String> {
        return implementedInterfaces
            .mapNotNull { interfaceFields[it] }
            .flatten()
            .toSet()
    }

    /**
     * Returns whether the type is a scalar type or one that requires a query projection
     */
    fun isScalar(typeName: String) =
        builtinScalars.contains(typeName) || enumFields.contains(typeName) || scalarLookup.contains(typeName)

    /**
     * Returns whether the inner type is a scalar type or one that requires a query projection
     */
    fun isScalar(type: Type<*>): Boolean {
        return when (type) {
            is TypeName -> isScalar(type.name)
            is ListType -> isScalar(type.type)
            is NonNullType -> isScalar(type.type)
            else -> throw UnsupportedOperationException(type::class.qualifiedName)
        }
    }

    /**
     * Takes a GQL field type and returns the appropriate kotlin type given all of the mappings defined in the schema and config
     */
    fun findReturnType(packageName: String, fieldType: Type<*>): KtTypeName {
        val visitor = object : NodeVisitorStub() {
            override fun visitTypeName(node: TypeName, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                context.setAccumulate(findKtTypeName(node, packageName).copy(nullable = true))
                return TraversalControl.CONTINUE
            }

            override fun visitListType(node: ListType, context: TraverserContext<Node<Node<*>>>): TraversalControl {
                val typeName = context.getCurrentAccumulate<KtTypeName>()
                context.setAccumulate(LIST.parameterizedBy(typeName).copy(nullable = true))
                return TraversalControl.CONTINUE
            }

            override fun visitNonNullType(
                node: NonNullType,
                context: TraverserContext<Node<Node<*>>>
            ): TraversalControl {
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

    /**
     * Takes a GQL interface type name and returns the appropriate kotlin type given all of the mappings defined in the schema and config
     */
    fun findKtInterfaceName(interfaceName: String, packageName: String): KtTypeName {
        // check config
        val mappedType = mappedTypes[interfaceName]
        if (mappedType != null) {
            return mappedType
        }

        return "$packageName.$interfaceName".toKtTypeName()
    }

    private fun findKtTypeName(typeName: TypeName, packageName: String): KtTypeName {
        // check config
        val mappedType = mappedTypes[typeName.name]
        if (mappedType != null) {
            return mappedType
        }

        // check schema scalar mappings
        val schemaType = scalarLookup[typeName.name]
        if (schemaType != null) {
            return schemaType
        }

        // check builtins
        val builtinType = builtinScalars[typeName.name]
        if (builtinType != null) {
            return builtinType
        }

        return "$packageName.${typeName.name}".toKtTypeName()
    }
}
