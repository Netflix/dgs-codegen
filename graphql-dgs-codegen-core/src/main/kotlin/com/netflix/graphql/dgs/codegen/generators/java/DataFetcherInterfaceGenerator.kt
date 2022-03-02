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

import com.netflix.graphql.dgs.*
import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.OperationTypes
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.netflix.graphql.dgs.codegen.generators.shared.isID
import com.netflix.graphql.dgs.codegen.isBaseType
import com.squareup.javapoet.*
import graphql.execution.UnknownOperationException
import graphql.language.Document
import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.ObjectTypeDefinition
import org.reactivestreams.Publisher
import java.util.concurrent.CompletableFuture
import javax.lang.model.element.Modifier

class DataFetcherInterfaceGenerator(private val config: CodeGenConfig, private val document: Document) {
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
        methods: List<MethodSpec>
    ): CodeGenResult {
        val javaType = TypeSpec.interfaceBuilder(objectTypeDefinition.name.capitalized() + "DataFetcher")
            .addModifiers(Modifier.PUBLIC)
            .addMethods(methods)

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()

        return CodeGenResult(javaDataFetchers = listOf(javaFile))
    }

    private fun createDataFetcherInterfaceMethodForOperation(field: FieldDefinition, parent: ObjectTypeDefinition): MethodSpec {
        val returnType = TypeUtils(config.packageNameTypes, config, document).findReturnType(field.type)

        val methodSpec = MethodSpec.methodBuilder(field.name)
            .returns(
                if (parent.name == OperationTypes.subscription)
                    ParameterizedTypeName.get(ClassName.get(Publisher::class.java), returnType)
                else
                    returnType
            )
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addAnnotation(
                when (parent.name) {
                    OperationTypes.query -> AnnotationSpec
                        .builder(DgsQuery::class.java)
                        .addMember(DgsQuery::field.name, "\$S", field.name)
                        .build()
                    OperationTypes.mutation -> AnnotationSpec
                        .builder(DgsMutation::class.java)
                        .addMember(DgsMutation::field.name, "\$S", field.name)
                        .build()
                    OperationTypes.subscription -> AnnotationSpec
                        .builder(DgsSubscription::class.java)
                        .addMember(DgsSubscription::field.name, "\$S", field.name)
                        .build()
                    else -> throw UnknownOperationException(parent.name)
                }
            )
            .addParameter(ParameterSpec.builder(DgsDataFetchingEnvironment::class.java, "dataFetchingEnvironment").build())

        generateInputParameter(field.inputValueDefinitions).forEach(methodSpec::addParameter)

        return methodSpec.build()
    }

    private fun createDataFetcherInterfaceMethodForType(field: FieldDefinition, parent: ObjectTypeDefinition): MethodSpec {
        val returnType = TypeUtils(config.packageNameTypes, config, document).findReturnType(field.type)

        val methodSpec = MethodSpec.methodBuilder(field.name)
            .returns(ParameterizedTypeName.get(ClassName.get(CompletableFuture::class.java), returnType))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addAnnotation(
                AnnotationSpec.builder(DgsData::class.java)
                    .addMember(DgsData::field.name, "\$S", field.name)
                    .addMember(DgsData::parentType.name, "\$S", parent.name)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(DgsDataFetchingEnvironment::class.java, "dataFetchingEnvironment").build()
            )

        generateInputParameter(field.inputValueDefinitions).forEach(methodSpec::addParameter)

        return methodSpec.build()
    }

    private fun generateInputParameter(inputValueDefinitions: List<InputValueDefinition>): List<ParameterSpec> {
        return inputValueDefinitions.map {
            ParameterSpec.builder(
                TypeUtils(config.packageNameTypes, config, document).findReturnType(it.type),
                it.name
            ).addAnnotation(
                AnnotationSpec.builder(InputArgument::class.java)
                    .addMember(InputArgument::name.name, "\$S", it.name)
                    .build()
            ).build()
        }
    }

    private fun getPackageName(): String {
        return config.packageNameDatafetchers
    }
}
