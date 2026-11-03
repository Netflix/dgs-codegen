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

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.SchemaIndex
import com.netflix.graphql.dgs.codegen.filterSkipped
import com.netflix.graphql.dgs.codegen.generators.kotlin.ReservedKeywordFilter
import com.netflix.graphql.dgs.codegen.generators.kotlin.addOptionalGeneratedAnnotation
import com.netflix.graphql.dgs.codegen.generators.kotlin.sanitizeKotlinIdentifier
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.collectAllFieldDefinitions
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.NameAllocator
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

private const val DEFAULT_ALIAS_PARAMETER_NAME = "_alias"
private const val DEFAULT_PROJECTION_PARAMETER_NAME = "_projection"

fun generateKotlin2ClientTypes(
    config: CodeGenConfig,
    document: Document,
): List<FileSpec> = generateKotlin2ClientTypes(config, SchemaIndex(document))

internal fun generateKotlin2ClientTypes(
    config: CodeGenConfig,
    schemaIndex: SchemaIndex,
): List<FileSpec> {
    if (!config.generateClientApi) {
        return emptyList()
    }

    val document = schemaIndex.document
    val typeLookup = Kotlin2TypeLookup(config, schemaIndex)

    val ivsParameter =
        ParameterSpec
            .builder("inputValueSerializer", InputValueSerializerInterface::class.asTypeName().copy(nullable = true))
            .defaultValue("null")
            .build()

    // create a projection class for every interface & data type
    val dataProjections =
        document
            .getDefinitionsOfType(ObjectTypeDefinition::class.java)
            .plus(document.getDefinitionsOfType(InterfaceTypeDefinition::class.java))
            .excludeSchemaTypeExtension()
            .filter { type -> type.directives.none { it.name == "skipcodegen" } && !typeLookup.isScalar(type.name) }
            .map { typeDefinition ->

                // the name of the type is used in every parameter & return value
                val typeName = ClassName(config.packageNameClient, "${typeDefinition.name}Projection")

                // get all fields defined on the type itself, extension types, and duplicate types
                val fields =
                    collectAllFieldDefinitions(typeDefinition, schemaIndex)
                        .filterSkipped()
                        .filter(ReservedKeywordFilter.filterInvalidNames)
                        .map { field ->

                            val kotlinName = sanitizeKotlinIdentifier(field.name)
                            val inputParameters = inputParameters(config, typeLookup, typeName, field.inputValueDefinitions)

                            val isScalar = typeLookup.isScalar(field.type)
                            val hasArgs = field.inputValueDefinitions.isNotEmpty()

                            when {
                                // scalars without args are just parameters that note the field is requested
                                isScalar && !hasArgs -> {
                                    PropertySpec
                                        .builder(
                                            name = kotlinName,
                                            type = typeName,
                                        ).getter(
                                            FunSpec
                                                .getterBuilder()
                                                .addStatement("field(%S)", field.name)
                                                .addStatement("return this")
                                                .build(),
                                        ).build()
                                }

                                // scalars with args are functions to take the args with no projection
                                isScalar && hasArgs -> {
                                    FunSpec
                                        .builder(kotlinName)
                                        .addParameters(inputParameters)
                                        .returns(typeName)
                                        .addCode(
                                            CodeBlock
                                                .builder()
                                                .add("field(%S", field.name)
                                                .addFieldArguments(field.inputValueDefinitions)
                                                .add(")\nreturn this")
                                                .build(),
                                        ).build()
                                }

                                // Complex fields need a selection set even without schema arguments.
                                else -> {
                                    val projectionTypeName = projectionTypeName(field.type)
                                    val nameAllocator =
                                        NameAllocator().apply {
                                            inputParameters.forEach { newName(it.name) }
                                        }
                                    val aliasParameter =
                                        ParameterSpec
                                            .builder(
                                                nameAllocator.newName(DEFAULT_ALIAS_PARAMETER_NAME),
                                                String::class.asTypeName().copy(nullable = true),
                                            ).defaultValue("null")
                                            .build()
                                    val serializerReference =
                                        if (inputParameters.any { it.name == ivsParameter.name }) {
                                            CodeBlock.of("this.%N", ivsParameter)
                                        } else {
                                            CodeBlock.of("%N", ivsParameter)
                                        }
                                    val (projectionType, projection) =
                                        projectionType(
                                            config.packageNameClient,
                                            projectionTypeName,
                                            nameAllocator.newName(DEFAULT_PROJECTION_PARAMETER_NAME),
                                        )

                                    FunSpec
                                        .builder(kotlinName)
                                        .addParameters(inputParameters)
                                        .addParameter(aliasParameter)
                                        .addParameter(projection)
                                        .returns(typeName)
                                        .addCode(
                                            CodeBlock
                                                .builder()
                                                .add(
                                                    "field(%N, %S, %T(%L), %N",
                                                    aliasParameter,
                                                    field.name,
                                                    projectionType,
                                                    serializerReference,
                                                    projection,
                                                ).addFieldArguments(field.inputValueDefinitions)
                                                .add(")\nreturn this")
                                                .build(),
                                        ).build()
                                }
                            }
                        }

                // add the `... on XXX` projection for implementors of this interface
                val implementors =
                    typeLookup
                        .interfaceImplementors(typeDefinition.name)
                        .map { subclassName -> onSubclassProjection(config.packageNameClient, typeName, subclassName) }

                // create the projection class
                val typeSpec =
                    TypeSpec
                        .classBuilder(typeName)
                        .addOptionalGeneratedAnnotation(config)
                        .primaryConstructor(FunSpec.constructorBuilder().addParameter(ivsParameter).build())
                        .superclass(GraphQLProjection::class)
                        .addSuperclassConstructorParameter("%N", ivsParameter)
                        // we can't ask for `__typename` on a `Subscription` object
                        .apply {
                            if (typeDefinition.name == "Subscription") {
                                addSuperclassConstructorParameter("defaultFields = emptySet()")
                            }
                        }.addProperties(fields.filterIsInstance<PropertySpec>())
                        .addFunctions(fields.filterIsInstance<FunSpec>())
                        .addFunctions(implementors)
                        .build()

                // return a file per type
                FileSpec.get(config.packageNameClient, typeSpec)
            }

    // create a projection for each union
    val unionProjections =
        document
            .getDefinitionsOfType(UnionTypeDefinition::class.java)
            .excludeSchemaTypeExtension()
            .filter { !it.shouldSkip(config) }
            .map { unionDefinition ->

                // the name of the type is used in every parameter & return value
                val typeName = ClassName(config.packageNameClient, "${unionDefinition.name}Projection")

                // get any members defined via schema extensions
                val extensionTypes = SchemaExtensionsUtils.findUnionExtensions(unionDefinition.name, schemaIndex)

                val implementations =
                    unionDefinition.memberTypes
                        .plus(extensionTypes.flatMap { it.memberTypes })

                val typeSpec =
                    TypeSpec
                        .classBuilder(typeName)
                        .addOptionalGeneratedAnnotation(config)
                        .primaryConstructor(FunSpec.constructorBuilder().addParameter(ivsParameter).build())
                        .superclass(GraphQLProjection::class)
                        .addSuperclassConstructorParameter("%N", ivsParameter)
                        .addFunctions(
                            implementations.map { subclass ->
                                onSubclassProjection(config.packageNameClient, typeName, (subclass as NamedNode<*>).name)
                            },
                        ).build()

                // return a file per type
                FileSpec.get(config.packageNameClient, typeSpec)
            }

    // create a top-level client class
    val topLevelTypes = typeLookup.operations.filterKeys { typeLookup.objectTypeNames.contains(it) }

    val clientSpec =
        TypeSpec
            .objectBuilder("DgsClient")
            .addOptionalGeneratedAnnotation(config)
            .addFunctions(
                topLevelTypes.map { (type, op) ->

                    val (projectionType, projection) = projectionType(config.packageNameClient, type)

                    FunSpec
                        .builder("build$type")
                        .addParameter(ivsParameter)
                        .addParameter(projection)
                        .returns(String::class)
                        .addStatement(
                            "return %T.asQuery(%T.%L, %T(%N), %N)",
                            GraphQLProjection::class.asTypeName(),
                            op::class.asTypeName(),
                            op.name,
                            projectionType,
                            ivsParameter,
                            projection,
                        ).build()
                },
            ).build()

    val clientFile = FileSpec.get(config.packageName, clientSpec)

    return dataProjections.plus(unionProjections).plus(clientFile)
}

// unpack the type to get the underlying type of the projection
private fun projectionTypeName(type: Type<*>): String =
    when (type) {
        is graphql.language.TypeName -> type.name
        is ListType -> projectionTypeName(type.type)
        is NonNullType -> projectionTypeName(type.type)
        else -> throw UnsupportedOperationException(type::class.qualifiedName)
    }

private fun projectionType(
    packageName: String,
    type: String,
    name: String = DEFAULT_PROJECTION_PARAMETER_NAME,
): Pair<ClassName, ParameterSpec> {
    val projectionType =
        ClassName(
            packageName = packageName,
            simpleNames = listOf("${type}Projection"),
        )

    val parameter =
        ParameterSpec(
            name = name,
            type =
                LambdaTypeName.get(
                    receiver = projectionType,
                    returnType = projectionType,
                ),
        )

    return projectionType to parameter
}

private fun inputParameters(
    config: CodeGenConfig,
    typeLookup: Kotlin2TypeLookup,
    typeName: ClassName,
    inputValueDefinitions: List<InputValueDefinition>,
): List<ParameterSpec> =
    inputValueDefinitions.map {
        val returnType = typeLookup.findReturnType(config.packageNameTypes, it.type)
        val kotlinName = sanitizeKotlinIdentifier(it.name)
        ParameterSpec
            .builder(kotlinName, returnType)
            .apply {
                if (returnType.isNullable) {
                    defaultValue("default<%T, %T>(%S)", typeName, returnType, it.name)
                }
            }.build()
    }

private fun CodeBlock.Builder.addFieldArguments(inputValueDefinitions: List<InputValueDefinition>): CodeBlock.Builder =
    apply {
        inputValueDefinitions.forEach {
            add(", %S to %N", it.name, sanitizeKotlinIdentifier(it.name))
        }
    }

private fun onSubclassProjection(
    packageName: String,
    typeName: ClassName,
    subclassName: String,
): FunSpec {
    val (projectionType, projection) = projectionType(packageName, subclassName)

    return FunSpec
        .builder("on$subclassName")
        .addParameter(projection)
        .returns(typeName)
        .addStatement("fragment(%S, %T(), %N)", subclassName, projectionType, projection)
        .addStatement("return this")
        .build()
}
