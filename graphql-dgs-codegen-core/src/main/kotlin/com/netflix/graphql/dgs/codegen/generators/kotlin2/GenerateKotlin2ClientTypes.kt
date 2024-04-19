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
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.filterSkipped
import com.netflix.graphql.dgs.codegen.generators.kotlin.ReservedKeywordFilter
import com.netflix.graphql.dgs.codegen.generators.kotlin.addOptionalGeneratedAnnotation
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import graphql.language.Document
import graphql.language.InputValueDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.ListType
import graphql.language.NamedNode
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.Type
import graphql.language.UnionTypeDefinition

fun generateKotlin2ClientTypes(
    config: CodeGenConfig,
    document: Document
): List<FileSpec> {
    if (!config.generateClientApi) {
        return emptyList()
    }

    val typeLookup = Kotlin2TypeLookup(config, document)

    // create a projection class for every interface & data type
    val dataProjections = document.getDefinitionsOfType(ObjectTypeDefinition::class.java)
        .plus(document.getDefinitionsOfType(InterfaceTypeDefinition::class.java))
        .excludeSchemaTypeExtension()
        .filter { type -> type.directives.none { it.name == "skipcodegen" } && !typeLookup.isScalar(type.name) }
        .map { typeDefinition ->

            // get any fields defined via schema extensions
            val extensionTypes = SchemaExtensionsUtils.findTypeExtensions(typeDefinition.name, document.definitions)

            // the name of the type is used in every parameter & return value
            val typeName = ClassName(config.packageNameClient, "${typeDefinition.name}Projection")

            // get all fields defined on the type itself or any extension types
            val fields = listOf(typeDefinition)
                .plus(extensionTypes)
                .flatMap { it.fieldDefinitions }
                .filterSkipped()
                .filter(ReservedKeywordFilter.filterInvalidNames)
                .map { field ->

                    val isScalar = typeLookup.isScalar(field.type)
                    val hasArgs = field.inputValueDefinitions.isNotEmpty()

                    when {
                        // scalars without args are just parameters that note the field is requested
                        isScalar && !hasArgs -> {
                            PropertySpec.builder(
                                name = field.name,
                                type = typeName
                            )
                                .getter(
                                    FunSpec.getterBuilder()
                                        .addStatement("field(%S)", field.name)
                                        .addStatement("return this")
                                        .build()
                                )
                                .build()
                        }

                        // scalars with args are functions to take the args with no projection
                        isScalar && hasArgs -> {
                            FunSpec.builder(field.name)
                                .addInputArgs(config, typeLookup, typeName, field.inputValueDefinitions)
                                .returns(typeName)
                                .addStatement(
                                    """field(%S%L)""",
                                    field.name,
                                    field.inputValueDefinitions.joinToString(" ") { """, "${it.name}" to ${it.name}""" }
                                )
                                .addStatement("return this")
                                .build()
                        }

                        // otherwise it's a projection with optional args
                        // !isScalar && hasArgs
                        else -> {
                            val projectionTypeName = projectionTypeName(field.type)
                            val (projectionType, projection) = projectionType(config.packageNameClient, projectionTypeName)

                            FunSpec.builder(field.name)
                                .addParameter(ParameterSpec.builder("_alias", String::class.asTypeName().copy(nullable = true)).defaultValue("null").build())
                                .addInputArgs(config, typeLookup, typeName, field.inputValueDefinitions)
                                .addParameter(projection)
                                .returns(typeName)
                                .addStatement(
                                    """field(_alias, %S, %T(), _projection%L)""",
                                    field.name,
                                    projectionType,
                                    field.inputValueDefinitions.joinToString(" ") { """, "${it.name}" to ${it.name}""" }
                                )
                                .addStatement("return this")
                                .build()
                        }
                    }
                }

            // add the `... on XXX` projection for implementors of this interface
            val implementors = typeLookup.interfaceImplementors(typeDefinition.name)
                .map { subclassName -> onSubclassProjection(config.packageNameClient, typeName, subclassName) }

            // create the projection class
            val typeSpec = TypeSpec.classBuilder(typeName)
                .addOptionalGeneratedAnnotation(config)
                .superclass(GraphQLProjection::class)
                // we can't ask for `__typename` on a `Subscription` object
                .apply {
                    if (typeDefinition.name == "Subscription") {
                        addSuperclassConstructorParameter("defaultFields = emptySet()")
                    }
                }
                .addProperties(fields.filterIsInstance<PropertySpec>())
                .addFunctions(fields.filterIsInstance<FunSpec>())
                .addFunctions(implementors)
                .build()

            // return a file per type
            FileSpec.get(config.packageNameClient, typeSpec)
        }

    // create a projection for each union
    val unionProjections = document.getDefinitionsOfType(UnionTypeDefinition::class.java)
        .excludeSchemaTypeExtension()
        .filter { !it.shouldSkip(config) }
        .map { unionDefinition ->

            // the name of the type is used in every parameter & return value
            val typeName = ClassName(config.packageNameClient, "${unionDefinition.name}Projection")

            // get any members defined via schema extensions
            val extensionTypes = SchemaExtensionsUtils.findUnionExtensions(unionDefinition.name, document.definitions)

            val implementations = unionDefinition.memberTypes
                .plus(extensionTypes.flatMap { it.memberTypes })

            val typeSpec = TypeSpec.classBuilder(typeName)
                .addOptionalGeneratedAnnotation(config)
                .superclass(GraphQLProjection::class)
                .addFunctions(
                    implementations.map { subclass ->
                        onSubclassProjection(config.packageNameClient, typeName, (subclass as NamedNode<*>).name)
                    }
                )
                .build()

            // return a file per type
            FileSpec.get(config.packageNameClient, typeSpec)
        }

    // create a top-level client class
    val topLevelTypes = typeLookup.operations.filterKeys { typeLookup.objectTypeNames.contains(it) }

    val clientSpec = TypeSpec.objectBuilder("DgsClient")
        .addOptionalGeneratedAnnotation(config)
        .addFunctions(
            topLevelTypes.map { (type, op) ->

                val (projectionType, projection) = projectionType(config.packageNameClient, type)

                FunSpec.builder("build$type")
                    .addParameter(projection)
                    .returns(String::class)
                    .addStatement(
                        """return %T.asQuery(%T.%L, %T(), _projection)""",
                        GraphQLProjection::class.asTypeName(),
                        op::class.asTypeName(),
                        op.name,
                        projectionType
                    )
                    .build()
            }
        )
        .build()

    val clientFile = FileSpec.get(config.packageName, clientSpec)

    return dataProjections.plus(unionProjections).plus(clientFile)
}

// unpack the type to get the underlying type of the projection
private fun projectionTypeName(type: Type<*>): String {
    return when (type) {
        is graphql.language.TypeName -> type.name
        is ListType -> projectionTypeName(type.type)
        is NonNullType -> projectionTypeName(type.type)
        else -> throw UnsupportedOperationException(type::class.qualifiedName)
    }
}

// create the `_projection = FooProjection.() -> FooProjection` parameter
private fun projectionType(packageName: String, type: String): Pair<ClassName, ParameterSpec> {
    val projectionType = ClassName(
        packageName = packageName,
        simpleNames = listOf("${type}Projection")
    )

    val parameter = ParameterSpec(
        name = "_projection",
        type = LambdaTypeName.get(
            receiver = projectionType,
            returnType = projectionType
        )
    )

    return projectionType to parameter
}

private fun FunSpec.Builder.addInputArgs(
    config: CodeGenConfig,
    typeLookup: Kotlin2TypeLookup,
    typeName: ClassName,
    inputValueDefinitions: List<InputValueDefinition>
): FunSpec.Builder {
    return this
        .addParameters(
            inputValueDefinitions.map {
                val returnType = typeLookup.findReturnType(config.packageNameTypes, it.type)
                ParameterSpec.builder(it.name, returnType)
                    .apply {
                        if (returnType.isNullable) {
                            defaultValue("default<%T, %T>(%S)", typeName, returnType, it.name)
                        }
                    }
                    .build()
            }
        )
}

private fun onSubclassProjection(
    packageName: String,
    typeName: ClassName,
    subclassName: String
): FunSpec {
    val (projectionType, projection) = projectionType(packageName, subclassName)

    return FunSpec.builder("on$subclassName")
        .addParameter(projection)
        .returns(typeName)
        .addStatement("""fragment(%S, %T(), _projection)""", subclassName, projectionType)
        .addStatement("return this")
        .build()
}
