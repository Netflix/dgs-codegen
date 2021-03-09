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
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import graphql.language.Document
import graphql.language.FieldDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import javax.lang.model.element.Modifier

class InterfaceGenerator(config: CodeGenConfig, private val document: Document) {

    private val packageName = config.packageNameTypes
    private val typeUtils = TypeUtils(packageName, config, document)

    fun generate(definition: InterfaceTypeDefinition): CodeGenResult {
        val javaType = TypeSpec.interfaceBuilder(definition.name)
                .addModifiers(Modifier.PUBLIC)

        definition.fieldDefinitions.forEach {
            addInterfaceMethod(it, javaType)
        }

        val implementations = document.getDefinitionsOfType(ObjectTypeDefinition::class.java).asSequence()
                .filter { node -> node.implements.any { it.isEqualTo(TypeName(definition.name)) } }
                .map { node -> ClassName.get(packageName, node.name) }
                .toList()

        if (implementations.isNotEmpty()) {
            javaType.addAnnotation(jsonTypeInfoAnnotation())
            javaType.addAnnotation(jsonSubTypeAnnotation(implementations))
        }

        val javaFile = JavaFile.builder(packageName, javaType.build()).build()

        return CodeGenResult(interfaces = listOf(javaFile))
    }

    private fun addInterfaceMethod(fieldDefinition: FieldDefinition, javaType: TypeSpec.Builder) {
        if (fieldDefinition.type is TypeName) {
            val returnType = typeUtils.findReturnType(fieldDefinition.type)

            val fieldName = fieldDefinition.name
            javaType.addMethod(MethodSpec.methodBuilder("get${fieldName.capitalize()}")
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(returnType)
                    .build())

            javaType.addMethod(MethodSpec.methodBuilder("set${fieldName.capitalize()}")
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .addParameter(returnType, fieldName)
                    .build())
        }
    }
}
