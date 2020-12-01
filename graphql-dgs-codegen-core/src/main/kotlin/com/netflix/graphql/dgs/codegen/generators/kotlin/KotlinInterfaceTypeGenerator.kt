package com.netflix.graphql.dgs.codegen.generators.kotlin

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.KotlinCodeGenResult
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.InterfaceTypeDefinition

class KotlinInterfaceTypeGenerator(private val config: CodeGenConfig) {
    private val typeUtils = KotlinTypeUtils(getPackageName(), config)

    @ExperimentalStdlibApi
    fun generate(definition: InterfaceTypeDefinition): KotlinCodeGenResult {
        val interfaceBuilder = TypeSpec.interfaceBuilder(definition.name)

        definition.fieldDefinitions.forEach { field ->
            val returnType = typeUtils.findReturnType(field.type)

            if(typeUtils.isNullable(field.type)) {
                val nullableReturnType = returnType.copy(nullable = true)
                interfaceBuilder.addProperty(field.name, nullableReturnType)
            } else {
                interfaceBuilder.addProperty(field.name, returnType)
            }
        }

        val typeSpec = interfaceBuilder.build()
        val fileSpec = FileSpec.builder(getPackageName(), typeSpec.name!!).addType(typeSpec).build()
        return KotlinCodeGenResult(interfaces =  listOf(fileSpec))
    }

    fun getPackageName(): String = config.packageName + ".types"
}