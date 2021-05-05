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
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import graphql.language.EnumTypeDefinition
import graphql.language.EnumValueDefinition
import javax.lang.model.element.Modifier

class EnumTypeGenerator(private val config: CodeGenConfig) {
    fun generate(definition: EnumTypeDefinition, extensions: List<EnumTypeDefinition>): CodeGenResult {
        val javaType = TypeSpec.enumBuilder(definition.name)
            .addModifiers(Modifier.PUBLIC)

        val mergedEnumDefinitions = definition.enumValueDefinitions + extensions.flatMap { it.enumValueDefinitions }

        mergedEnumDefinitions.forEach {
            addEnum(it, javaType)
        }

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()

        return CodeGenResult(enumTypes = listOf(javaFile))
    }

    private fun addEnum(enumVal: EnumValueDefinition, javaType: TypeSpec.Builder) { javaType.addEnumConstant(enumVal.name); }
    fun getPackageName(): String {
        return config.packageNameTypes
    }
}
