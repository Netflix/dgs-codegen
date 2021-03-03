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

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.KotlinCodeGenResult
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.Document
import graphql.language.InterfaceTypeDefinition
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName

class KotlinInterfaceTypeGenerator(config: CodeGenConfig) {

    private val packageName = config.packageNameTypes
    private val typeUtils = KotlinTypeUtils(packageName, config)

    fun generate(definition: InterfaceTypeDefinition, document: Document): KotlinCodeGenResult {
        val interfaceBuilder = TypeSpec.interfaceBuilder(definition.name)

        definition.fieldDefinitions.forEach { field ->
            val returnType = typeUtils.findReturnType(field.type)
            interfaceBuilder.addProperty(field.name, returnType)
        }

        val implementations = document.getDefinitionsOfType(ObjectTypeDefinition::class.java).asSequence()
                .filter { node -> node.implements.any { it.isEqualTo(TypeName(definition.name)) } }
                .map { node -> ClassName(packageName, node.name) }
                .toList()

        if (implementations.isNotEmpty()) {
            interfaceBuilder.addAnnotation(jsonTypeInfoAnnotation())
            interfaceBuilder.addAnnotation(jsonSubTypesAnnotation(implementations))
        }

        val fileSpec = FileSpec.get(packageName, interfaceBuilder.build())
        return KotlinCodeGenResult(interfaces = listOf(fileSpec))
    }
}
