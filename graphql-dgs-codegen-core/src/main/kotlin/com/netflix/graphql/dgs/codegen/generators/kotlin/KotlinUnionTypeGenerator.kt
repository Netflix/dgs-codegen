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
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.templatedClassName
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.Document
import graphql.language.TypeName
import graphql.language.UnionTypeDefinition
import graphql.language.UnionTypeExtensionDefinition

class KotlinUnionTypeGenerator(private val config: CodeGenConfig, document: Document) {

    private val packageName = config.packageNameTypes
    private val typeUtils = KotlinTypeUtils(packageName, config, document)

    fun generate(definition: UnionTypeDefinition, extensions: List<UnionTypeExtensionDefinition>): CodeGenResult {
        if (definition.shouldSkip(config)) {
            return CodeGenResult.EMPTY
        }

        val interfaceBuilder = TypeSpec.interfaceBuilder(definition.name)
            .addOptionalGeneratedAnnotation(config)

        val memberTypes = definition.memberTypes.plus(extensions.flatMap { it.memberTypes }).asSequence()
            .filterIsInstance<TypeName>()
            .associate {
                it.name to typeUtils
                    .findKtInterfaceName(it.templatedClassName(config.nameTemplate), packageName) as? ClassName
            }
            .mapNotNull { (name, className) -> className?.let { name to it } }
            .toMap()

        if (memberTypes.isNotEmpty()) {
            interfaceBuilder.addAnnotation(jsonTypeInfoAnnotation())
            interfaceBuilder.addAnnotation(jsonSubTypesAnnotation(memberTypes))
        }

        interfaceBuilder.addType(TypeSpec.companionObjectBuilder().addOptionalGeneratedAnnotation(config).build())

        val fileSpec = FileSpec.get(packageName, interfaceBuilder.build())
        return CodeGenResult(kotlinInterfaces = listOf(fileSpec))
    }
}
