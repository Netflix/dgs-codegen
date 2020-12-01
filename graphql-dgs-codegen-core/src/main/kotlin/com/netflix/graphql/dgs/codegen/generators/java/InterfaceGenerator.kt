package com.netflix.graphql.dgs.codegen.generators.java

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import graphql.language.FieldDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.TypeName
import javax.lang.model.element.Modifier

class InterfaceGenerator(private val config: CodeGenConfig) {
    private val typeUtils = TypeUtils(getPackageName(), config)

    fun generate(definition: InterfaceTypeDefinition): CodeGenResult {
        val javaType = TypeSpec.interfaceBuilder(definition.name)
                .addModifiers(Modifier.PUBLIC)

        definition.fieldDefinitions.forEach {
            addInterfaceMethod(it, javaType)
        }

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()

        return CodeGenResult(interfaces = listOf(javaFile))
    }

    private fun addInterfaceMethod(fieldDefinition: FieldDefinition, javaType: TypeSpec.Builder) {
        if (fieldDefinition.type is TypeName) {
            val returnType = typeUtils.findReturnType(fieldDefinition.type)

            val getterName = "get${fieldDefinition.name[0].toUpperCase()}${fieldDefinition.name.substring(1)}"
            javaType.addMethod(MethodSpec.methodBuilder(getterName).addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC).returns(returnType).build())

            val setterName = "set${fieldDefinition.name[0].toUpperCase()}${fieldDefinition.name.substring(1)}"
            javaType.addMethod(MethodSpec.methodBuilder(setterName).addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC).addParameter(returnType, setterName).build())
        }
    }

    fun getPackageName(): String {
        return config.packageName + ".types"
    }
}