package com.netflix.graphql.dgs.codegen.generators.java

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import graphql.language.EnumTypeDefinition
import graphql.language.EnumValueDefinition
import javax.lang.model.element.Modifier

class EnumTypeGenerator(private val config: CodeGenConfig) {
    fun generate(definition: EnumTypeDefinition): CodeGenResult {
        val javaType = TypeSpec.enumBuilder(definition.name)
                .addModifiers(Modifier.PUBLIC)

        definition.enumValueDefinitions.forEach {
            addEnum(it, javaType)
        }

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()

        return CodeGenResult(enumTypes = listOf(javaFile))
    }

    private fun addEnum(enumVal: EnumValueDefinition, javaType: TypeSpec.Builder) { javaType.addEnumConstant(enumVal.name); }
    fun getPackageName(): String {
        return config.packageName + ".types"
    }
}