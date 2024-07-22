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
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import graphql.language.Document
import graphql.language.TypeName
import graphql.language.UnionTypeDefinition
import graphql.language.UnionTypeExtensionDefinition
import javax.lang.model.element.Modifier

class UnionTypeGenerator(private val config: CodeGenConfig, private val document: Document) {

    val packageName = config.packageNameTypes
    private val typeUtils = TypeUtils(packageName, config, document)

    fun generate(definition: UnionTypeDefinition, extensions: List<UnionTypeExtensionDefinition>): CodeGenResult {
        if (definition.shouldSkip(config)) {
            return CodeGenResult.EMPTY
        }

        val javaType = TypeSpec.interfaceBuilder(definition.name)
            .addOptionalGeneratedAnnotation(config)
            .addModifiers(Modifier.PUBLIC)

        val memberTypes = definition.memberTypes.asSequence().plus(extensions.asSequence().flatMap { it.memberTypes })
            .filterIsInstance<TypeName>()
            .map { member ->
                typeUtils.findJavaInterfaceName(member.name, packageName)
            }
            .filterIsInstance<ClassName>()
            .toList()

        if (memberTypes.isNotEmpty()) {
            javaType.addAnnotation(jsonTypeInfoAnnotation())
            javaType.addAnnotation(jsonSubTypeAnnotation(memberTypes))
        }

        val javaFile = JavaFile.builder(packageName, javaType.build()).build()
        return CodeGenResult(javaInterfaces = listOf(javaFile))
    }
}
