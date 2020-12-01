package com.netflix.graphql.dgs.codegen.generators.kotlin

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.KotlinCodeGenResult
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.EnumTypeDefinition

class KotlinEnumTypeGenerator(private val config: CodeGenConfig) {
    fun generate(definition: EnumTypeDefinition): KotlinCodeGenResult {
        val kotlinType = TypeSpec.classBuilder(definition.name).addModifiers(KModifier.ENUM)

        definition.enumValueDefinitions.forEach {
            kotlinType.addEnumConstant(it.name)
        }

        val typeSpec = kotlinType.build()
        val fileSpec = FileSpec.builder(getPackageName(), typeSpec.name!!).addType(typeSpec).build()
        return KotlinCodeGenResult(enumTypes = listOf(fileSpec))
    }

    fun getPackageName(): String {
        return config.packageName + ".types"
    }
}