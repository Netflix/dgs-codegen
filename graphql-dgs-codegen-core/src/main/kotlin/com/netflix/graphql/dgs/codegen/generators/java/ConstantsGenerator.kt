package com.netflix.graphql.dgs.codegen.generators.java

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import graphql.language.*
import javax.lang.model.element.Modifier

class ConstantsGenerator(private val config: CodeGenConfig, private val document: Document) {
    fun generate(): CodeGenResult {
        val javaType = TypeSpec.classBuilder("DgsConstants")
                .addModifiers(Modifier.PUBLIC)

        document.definitions.filterIsInstance<ObjectTypeDefinition>().filter { it !is ObjectTypeExtensionDefinition } .map {
            val constantsType = createConstantTypeBuilder(it.name)

            val extensions = findExtensions(it.name, document.definitions)
            val fields = it.fieldDefinitions.plus(extensions.flatMap { it.fieldDefinitions })

            constantsType.addField(FieldSpec.builder(TypeName.get(String::class.java), "TYPE_NAME").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""${it.name}"""").build())

            fields.forEach {field ->
                addFieldNameConstant(constantsType, field.name)
            }

            javaType.addType(constantsType.build())
        }

        document.definitions.filterIsInstance<InputObjectTypeDefinition>().filter { it !is InputObjectTypeExtensionDefinition }.map {
            val constantsType = createConstantTypeBuilder(it.name)
            constantsType.addField(FieldSpec.builder(TypeName.get(String::class.java), "TYPE_NAME").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""${it.name}"""").build())

            val extensions = findInputExtensions(it.name, document.definitions)
            val fields = it.inputValueDefinitions.plus(extensions.flatMap { it.inputValueDefinitions })

            fields.forEach {field ->
                addFieldNameConstant(constantsType, field.name)
            }

            javaType.addType(constantsType.build())
        }

        document.definitions.filterIsInstance<InterfaceTypeDefinition>().map {
            val constantsType = createConstantTypeBuilder(it.name)

            constantsType.addField(FieldSpec.builder(TypeName.get(String::class.java), "TYPE_NAME").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""${it.name}"""").build())
            it.fieldDefinitions.forEach {field ->
                addFieldNameConstant(constantsType, field.name)
            }

            javaType.addType(constantsType.build())
        }

        document.definitions.filterIsInstance<UnionTypeDefinition>().map {
            val constantsType = createConstantTypeBuilder(it.name)
            constantsType.addField(FieldSpec.builder(TypeName.get(String::class.java), "TYPE_NAME").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""${it.name}"""").build())
        }

        if(document.definitions.firstOrNull { it is ObjectTypeDefinition && it.name == "Query" } != null) {
            javaType.addField(FieldSpec.builder(TypeName.get(String::class.java), "QUERY_TYPE").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""Query"""").build())
        }
        if(document.definitions.firstOrNull { it is ObjectTypeDefinition && it.name == "MUTATION" } != null) {
            javaType.addField(FieldSpec.builder(TypeName.get(String::class.java), "MUTATION_TYPE").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""Mutation"""").build())
        }
        if(document.definitions.firstOrNull { it is ObjectTypeDefinition && it.name == "Subscription" } != null) {
            javaType.addField(FieldSpec.builder(TypeName.get(String::class.java), "SUBSCRIPTION_TYPE").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""Subscription"""").build())
        }

        val javaFile = JavaFile.builder(config.packageName, javaType.build()).build()
        return CodeGenResult(constants = listOf(javaFile))
    }

    private fun createConstantTypeBuilder(name: String): TypeSpec.Builder {
        return TypeSpec.classBuilder(name.toUpperCase())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

    }

    private fun addFieldNameConstant(constantsType: TypeSpec.Builder, fieldName: String) {
        constantsType.addField(FieldSpec.builder(TypeName.get(String::class.java), fieldName.capitalize()).addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer(""""$fieldName"""").build())
    }

    private fun findExtensions(name: String, definitions: List<Definition<Definition<*>>>) =
            definitions.filterIsInstance<ObjectTypeExtensionDefinition>().filter { name == it.name }

    private fun findInputExtensions(name: String, definitions: List<Definition<Definition<*>>>) =
            definitions.filterIsInstance<InputObjectTypeExtensionDefinition>().filter { name == it.name }
}