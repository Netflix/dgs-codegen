package com.netflix.graphql.dgs.codegen.generators.kotlin

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.KotlinCodeGenResult
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.UnionTypeDefinition

class KotlinUnionTypeGenerator(private val config: CodeGenConfig) {
    fun generate(definition: UnionTypeDefinition): KotlinCodeGenResult {
        val interfaceBuilder = TypeSpec.interfaceBuilder(definition.name)

        val typeSpec = interfaceBuilder.build()
        val fileSpec = FileSpec.builder(getPackageName(), typeSpec.name!!).addType(typeSpec).build()
        return KotlinCodeGenResult(interfaces =  listOf(fileSpec))
    }

    fun getPackageName(): String = config.packageName + ".types"
}