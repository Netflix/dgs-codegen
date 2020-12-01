package com.netflix.graphql.dgs.codegen.generators.java

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import graphql.language.UnionTypeDefinition
import javax.lang.model.element.Modifier

class UnionTypeGenerator(private val config: CodeGenConfig) {
    fun generate(definition: UnionTypeDefinition): CodeGenResult {
        val javaType = TypeSpec.interfaceBuilder(definition.name)
                .addModifiers(Modifier.PUBLIC)

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(interfaces = listOf(javaFile))
    }

    fun getPackageName(): String {
        return config.packageName + ".types"
    }
}