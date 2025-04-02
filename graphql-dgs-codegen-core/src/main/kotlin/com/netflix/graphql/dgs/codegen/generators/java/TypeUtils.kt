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
import com.netflix.graphql.dgs.codegen.generators.shared.findSchemaTypeMapping
import com.netflix.graphql.dgs.codegen.generators.shared.parseMappedType
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.WildcardTypeName
import graphql.language.*
import graphql.language.TypeName
import graphql.relay.PageInfo
import graphql.util.TraversalControl
import graphql.util.TraverserContext
import java.time.*
import java.util.*
import com.squareup.javapoet.TypeName as JavaTypeName

class TypeUtils(
    private val packageName: String,
    private val config: CodeGenConfig,
    private val document: Document,
) {
    companion object {
        private val commonScalars =
            mapOf<String, JavaTypeName>(
                "LocalTime" to ClassName.get(LocalTime::class.java),
                "LocalDate" to ClassName.get(LocalDate::class.java),
                "LocalDateTime" to ClassName.get(LocalDateTime::class.java),
                "TimeZone" to ClassName.get(String::class.java),
                "Date" to ClassName.get(LocalDate::class.java),
                "DateTime" to ClassName.get(OffsetDateTime::class.java),
                "Currency" to ClassName.get(Currency::class.java),
                "Instant" to ClassName.get(Instant::class.java),
                "RelayPageInfo" to ClassName.get(PageInfo::class.java),
                "PageInfo" to ClassName.get(PageInfo::class.java),
                "PresignedUrlResponse" to ClassName.get("com.netflix.graphql.types.core.resolvers", "PresignedUrlResponse"),
                "Header" to ClassName.get("com.netflix.graphql.types.core.resolvers", "PresignedUrlResponse", "Header"),
                "Upload" to ClassName.get("org.springframework.web.multipart", "MultipartFile"),
            )
        const val GET_CLASS = "getClass"
        const val SET_CLASS = "setClass"
    }

    fun qualifyName(name: String): String = "$packageName.$name"

    fun findReturnType(
        fieldType: Type<*>,
        useInterfaceType: Boolean = false,
        useWildcardType: Boolean = false,
    ): JavaTypeName {
        val visitor =
            object : NodeVisitorStub() {
                override fun visitTypeName(
                    node: TypeName,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl {
                    val typeName = node.toJavaTypeName(useInterfaceType)
                    val boxed = boxType(typeName)
                    context.setAccumulate(boxed)
                    return TraversalControl.CONTINUE
                }

                override fun visitListType(
                    node: ListType,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl {
                    val typeName = context.getCurrentAccumulate<JavaTypeName>()
                    val boxed = boxType(typeName)

                    var canUseWildcardType = false
                    if (useWildcardType) {
                        if (typeName is ClassName) {
                            if (document.definitions
                                    .filterIsInstance<ObjectTypeDefinition>()
                                    .any { e -> "I${e.name}" == typeName.simpleName() } ||
                                (
                                    config.generateInterfaces &&
                                        document.definitions.filterIsInstance<InterfaceTypeDefinition>().any { e ->
                                            "${e.name}" ==
                                                typeName.simpleName()
                                        }
                                )
                            ) {
                                canUseWildcardType = true
                            }
                        }
                    }

                    val parameterizedTypeName =
                        if (canUseWildcardType) {
                            val wildcardTypeName: WildcardTypeName = WildcardTypeName.subtypeOf(boxed)
                            ParameterizedTypeName.get(ClassName.get(List::class.java), wildcardTypeName)
                        } else {
                            ParameterizedTypeName.get(ClassName.get(List::class.java), boxed)
                        }
                    context.setAccumulate(parameterizedTypeName)
                    return TraversalControl.CONTINUE
                }

                override fun visitNonNullType(
                    node: NonNullType,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl {
                    val typeName = context.getCurrentAccumulate<JavaTypeName>()
                    val accumulate =
                        if (config.generateBoxedTypes) {
                            boxType(typeName)
                        } else {
                            unboxType(typeName)
                        }
                    context.setAccumulate(accumulate)
                    return TraversalControl.CONTINUE
                }

                override fun visitNode(
                    node: Node<*>,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl = throw AssertionError("Unknown field type: $node")
            }
        return NodeTraverser().postOrder(visitor, fieldType) as JavaTypeName
    }

    /**
     * Takes a GQL interface type name and returns the appropriate kotlin type given all of the mappings defined in the schema and config
     */
    fun findJavaInterfaceName(
        interfaceName: String,
        packageName: String,
    ): JavaTypeName {
        // check config
        if (interfaceName in config.typeMapping) {
            val mappedType = config.typeMapping.getValue(interfaceName)

            return parseMappedType(
                mappedType = mappedType,
                toTypeName = String::toTypeName,
                parameterize = { current ->
                    ParameterizedTypeName.get(
                        current.first as ClassName,
                        *current.second.toTypedArray(),
                    )
                },
                onCloseBracketCallBack = { current, typeString -> current.second.add(typeString.toTypeName(true)) },
            )
        }

        return ClassName.get(packageName, interfaceName)
    }

    private fun unboxType(typeName: JavaTypeName): JavaTypeName =
        if (typeName.isBoxedPrimitive) {
            typeName.unbox()
        } else {
            typeName
        }

    private fun boxType(typeName: JavaTypeName): JavaTypeName =
        if (typeName.isPrimitive) {
            typeName.box()
        } else {
            typeName
        }

    private fun TypeName.toJavaTypeName(useInterfaceType: Boolean): JavaTypeName {
        if (name in config.typeMapping) {
            val mappedType = config.typeMapping.getValue(name)

            return parseMappedType(
                mappedType = mappedType,
                toTypeName = String::toTypeName,
                parameterize = { current ->
                    ParameterizedTypeName.get(
                        current.first as ClassName,
                        *current.second.toTypedArray(),
                    )
                },
                onCloseBracketCallBack = { current, typeString -> current.second.add(typeString.toTypeName(true)) },
            )
        }

        val schemaType = findSchemaTypeMapping(document, name)
        if (schemaType != null) {
            return schemaType.toTypeName()
        }

        if (name in commonScalars && !isFieldTypeDefinedInDocument(name)) {
            return commonScalars.getValue(name)
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
                var simpleName = name
                if (useInterfaceType &&
                    !document.definitions.filterIsInstance<EnumTypeDefinition>().any { e -> e.name == name } &&
                    !document.definitions.filterIsInstance<UnionTypeDefinition>().any { e -> e.name == name } &&
                    !isFieldTypeAnInterface(this)
                ) {
                    simpleName = "I$name"
                }
                ClassName.get(packageName, simpleName)
            }
        }
    }

    // Return the raw type for nullable, non-nullable and parameterized fields
    fun findInnerType(fieldType: Type<*>): TypeName {
        val visitor =
            object : NodeVisitorStub() {
                override fun visitTypeName(
                    node: TypeName,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl {
                    context.setAccumulate(node)
                    return TraversalControl.CONTINUE
                }

                override fun visitListType(
                    node: ListType,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl {
                    val typeName = context.getCurrentAccumulate<TypeName>()

                    context.setAccumulate(typeName)
                    return TraversalControl.CONTINUE
                }

                override fun visitNonNullType(
                    node: NonNullType,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl {
                    val typeName = context.getCurrentAccumulate<TypeName>()
                    context.setAccumulate(typeName)
                    return TraversalControl.CONTINUE
                }

                override fun visitNode(
                    node: Node<*>,
                    context: TraverserContext<Node<Node<*>>>,
                ): TraversalControl = throw AssertionError("Unknown field type: $node")
            }
        return NodeTraverser().postOrder(visitor, fieldType) as TypeName
    }

    private fun isFieldTypeAnInterface(fieldDefinitionType: TypeName): Boolean =
        document
            .getDefinitionsOfType(InterfaceTypeDefinition::class.java)
            .any { node -> node.name == findInnerType(fieldDefinitionType).name }

    fun transformIfDefaultClassMethodExists(
        originName: String,
        defaultMethodName: String,
    ): String {
        return if (defaultMethodName == originName) {
            return originName + "Field"
        } else {
            originName
        }
    }

    private fun isFieldTypeDefinedInDocument(name: String): Boolean =
        document.definitions.filterIsInstance<ObjectTypeDefinition>().any { e -> e.name == name } ||
            document.definitions.filterIsInstance<EnumTypeDefinition>().any { e -> e.name == name }
}
