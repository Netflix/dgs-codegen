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

import com.netflix.graphql.dgs.*
import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.OperationTypes
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.netflix.graphql.dgs.codegen.generators.shared.isID
import com.netflix.graphql.dgs.codegen.isBaseType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import graphql.execution.UnknownOperationException
import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.ObjectTypeDefinition
import org.reactivestreams.Publisher
import java.util.concurrent.CompletableFuture

class KotlinDataFetcherInterfaceGenerator(private val config: CodeGenConfig) {
    fun generate(objectTypeDefinition: ObjectTypeDefinition): CodeGenResult {
        val isOperationType = OperationTypes.isOperationType(objectTypeDefinition.name)
        val fields = if (!isOperationType) {
            objectTypeDefinition.fieldDefinitions.filter { !it.type.isBaseType() && !it.type.isID() }
        } else {
            objectTypeDefinition.fieldDefinitions
        }

        if (fields.isNullOrEmpty()) {
            return CodeGenResult()
        }

        return createDataFetcherInterface(
            objectTypeDefinition,
            if (!isOperationType) {
                fields.map { field ->
                    createDataFetcherInterfaceMethodForType(field, objectTypeDefinition)
                }
            } else {
                fields.map { field ->
                    createDataFetcherInterfaceMethodForOperation(field, objectTypeDefinition)
                }
            }
        )
    }

    private fun createDataFetcherInterface(
        objectTypeDefinition: ObjectTypeDefinition,
        methods: List<FunSpec>
    ): CodeGenResult {
        val kotlinType = TypeSpec.interfaceBuilder(objectTypeDefinition.name.capitalized() + "DataFetcher")
            .addModifiers(KModifier.PUBLIC)
            .addFunctions(methods)
            .build()

        val kotlinFile = FileSpec.builder(getPackageName(), objectTypeDefinition.name.capitalized() + "DataFetcher")
            .addType(kotlinType)
            .build()

        return CodeGenResult(kotlinDataFetchers = listOf(kotlinFile))
    }

    private fun createDataFetcherInterfaceMethodForOperation(field: FieldDefinition, parent: ObjectTypeDefinition): FunSpec {
        val returnType = KotlinTypeUtils(config.packageNameTypes, config).findReturnType(field.type)

        val methodSpec = FunSpec.builder(field.name)
            .returns(
                if (parent.name == OperationTypes.subscription)
                    Publisher::class.asTypeName().parameterizedBy(returnType)
                else
                    returnType
            )
            .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
            .addAnnotation(
                when (parent.name) {
                    OperationTypes.query -> AnnotationSpec
                        .builder(DgsQuery::class)
                        .addMember("%L = %S", DgsQuery::field.name, field.name)
                        .build()
                    OperationTypes.mutation -> AnnotationSpec
                        .builder(DgsMutation::class)
                        .addMember("%L = %S", DgsMutation::field.name, field.name)
                        .build()
                    OperationTypes.subscription -> AnnotationSpec
                        .builder(DgsSubscription::class)
                        .addMember("%L = %S", DgsSubscription::field.name, field.name)
                        .build()
                    else -> throw UnknownOperationException(parent.name)
                }
            )
            .addParameter(ParameterSpec.builder("dataFetchingEnvironment", DgsDataFetchingEnvironment::class).build())

        generateInputParameter(field.inputValueDefinitions).forEach(methodSpec::addParameter)

        return methodSpec.build()
    }

    private fun createDataFetcherInterfaceMethodForType(field: FieldDefinition, parent: ObjectTypeDefinition): FunSpec {
        val returnType = KotlinTypeUtils(config.packageNameTypes, config).findReturnType(field.type)

        val methodSpec = FunSpec.builder(field.name)
            .returns(CompletableFuture::class.asTypeName().parameterizedBy(returnType))
            .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
            .addAnnotation(
                AnnotationSpec.builder(DgsData::class)
                    .addMember("%L = %S", DgsData::field.name, field.name)
                    .addMember("%L = %S", DgsData::parentType.name, parent.name)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder("dataFetchingEnvironment", DgsDataFetchingEnvironment::class).build()
            )

        generateInputParameter(field.inputValueDefinitions).forEach(methodSpec::addParameter)

        return methodSpec.build()
    }

    private fun generateInputParameter(inputValueDefinitions: List<InputValueDefinition>): List<ParameterSpec> {
        return inputValueDefinitions.map {
            ParameterSpec.builder(
                it.name,
                KotlinTypeUtils(config.packageNameTypes, config).findReturnType(it.type)
            ).addAnnotation(
                AnnotationSpec.builder(InputArgument::class)
                    .addMember("%L = %S", InputArgument::name.name, it.name)
                    .build()
            ).build()
        }
    }

    private fun getPackageName(): String {
        return config.packageNameDatafetchers
    }
}