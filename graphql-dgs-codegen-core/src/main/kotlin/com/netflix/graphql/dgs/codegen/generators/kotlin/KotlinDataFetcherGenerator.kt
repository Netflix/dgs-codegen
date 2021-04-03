package com.netflix.graphql.dgs.codegen.generators.kotlin

import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.KotlinCodeGenResult
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import graphql.language.FieldDefinition
import graphql.language.ObjectTypeDefinition
import graphql.schema.DataFetchingEnvironment

class KotlinDataFetcherGenerator(private val config: CodeGenConfig) {

    private val packageName = config.packageNameDatafetchers
    private val typeUtils = KotlinTypeUtils(config.packageNameTypes, config)

    fun generate(query: ObjectTypeDefinition): KotlinCodeGenResult =
        query.fieldDefinitions
            .map { generateField(it) }
            .fold(KotlinCodeGenResult()) { left, right -> left.merge(right) }

    private fun generateField(field: FieldDefinition): KotlinCodeGenResult {
        val fieldName = field.name.substring(0, 1).toUpperCase() + field.name.substring(1)
        val className = fieldName + "Datafetcher"

        val returnType = typeUtils.findReturnType(field.type)

        val methodSpec = FunSpec.builder("get$fieldName")
            .addAnnotation(AnnotationSpec.builder(DgsData::class).addMember("parentType", "\$S", "Query").addMember("field", "\$S", field.name).build())
            .addModifiers(KModifier.ABSTRACT)
            .also { builder ->
                field.inputValueDefinitions.forEach {
                    val inputType: TypeName = typeUtils.findReturnType(it.type)
                    builder.addParameter(it.name, inputType)
                }
            }
            .addParameter("dataFetchingEnvironment", DataFetchingEnvironment::class)
            .returns(returnType)
            .build()

        val interfaceBuilder = TypeSpec.interfaceBuilder(className)
            .addFunction(methodSpec)
            .build()

        val fileSpec = FileSpec.get(packageName, interfaceBuilder)

        return KotlinCodeGenResult(dataFetchers = listOf(fileSpec))
    }
}
