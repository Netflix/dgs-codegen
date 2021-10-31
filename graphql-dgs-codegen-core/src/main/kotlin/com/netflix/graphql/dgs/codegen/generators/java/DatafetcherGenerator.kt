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

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import graphql.language.Document
import graphql.language.FieldDefinition
import graphql.language.ObjectTypeDefinition
import graphql.schema.DataFetchingEnvironment
import javax.lang.model.element.Modifier

class DatafetcherGenerator(private val config: CodeGenConfig, private val document: Document) {
    fun generate(query: ObjectTypeDefinition): CodeGenResult {

        return query.fieldDefinitions.asSequence().map { field ->
            createDatafetcher(field)
        }.fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
    }

    private fun createDatafetcher(field: FieldDefinition): CodeGenResult {
        val fieldName = field.name.capitalized()
        val clazzName = fieldName + "Datafetcher"

        val returnType = TypeUtils(config.packageNameTypes, config, document).findReturnType(field.type)

        val returnValue: Any = when (returnType.toString()) {
            "java.lang.String" -> "\"\""
            "int" -> 0
            "long" -> 0
            "double" -> 0
            "boolean" -> "false"
            else -> "null"
        }

        val methodSpec = MethodSpec.methodBuilder("get$fieldName")
            .returns(returnType)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(AnnotationSpec.builder(DgsData::class.java).addMember("parentType", "\$S", "Query").addMember("field", "\$S", field.name).build())
            .addParameter(ParameterSpec.builder(DataFetchingEnvironment::class.java, "dataFetchingEnvironment").build())
            .addStatement("return $returnValue")

        val javaType = TypeSpec.classBuilder(clazzName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(DgsComponent::class.java)
            .addMethod(methodSpec.build())

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()

        return CodeGenResult(javaDataFetchers = listOf(javaFile))
    }

    private fun getPackageName(): String {
        return config.packageNameDatafetchers
    }
}
