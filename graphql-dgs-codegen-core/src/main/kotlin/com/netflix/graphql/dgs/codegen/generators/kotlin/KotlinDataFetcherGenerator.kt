package com.netflix.graphql.dgs.codegen.generators.kotlin

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.Document
import graphql.language.FieldDefinition
import graphql.language.ObjectTypeDefinition
import graphql.schema.DataFetchingEnvironment

class KotlinDataFetcherGenerator(private val config: CodeGenConfig, private val document: Document) {

    private val packageName = config.packageNameDatafetchers
    private val typeUtils = KotlinTypeUtils(config.packageNameTypes, config, document)
    private val dsgConstantsPackageName = config.packageName

    fun generate(query: ObjectTypeDefinition): CodeGenResult =
        query.fieldDefinitions
            .map { generateField(it) }
            .fold(CodeGenResult()) { left, right -> left.merge(right) }

    private fun generateField(field: FieldDefinition): CodeGenResult {
        val fieldName = field.name.capitalized()
        val className = fieldName + "Datafetcher"

        val returnType = typeUtils.findReturnType(field.type)

        val dsgDataAnnotation = AnnotationSpec.builder(DgsData::class)
            .addMember("parentType = DgsConstants.QUERY.TYPE_NAME")
            .addMember("field = DgsConstants.QUERY.$fieldName")
            .build()

        val methodSpec = FunSpec.builder("get$fieldName")
            .addAnnotation(dsgDataAnnotation)
            .addModifiers(KModifier.ABSTRACT)
            .addInputArguments(field)
            .addParameter("dataFetchingEnvironment", DataFetchingEnvironment::class)
            .returns(returnType)
            .build()

        val interfaceBuilder = TypeSpec.interfaceBuilder(className)
            .addAnnotation(DgsComponent::class)
            .addFunction(methodSpec)
            .build()

        val fileSpec = FileSpec.builder(packageName, interfaceBuilder.name!!)
            .addType(interfaceBuilder)
            .addImport(dsgConstantsPackageName, "DgsConstants")
            .build()

        return CodeGenResult(kotlinDataFetchers = listOf(fileSpec))
    }

    private fun FunSpec.Builder.addInputArguments(field: FieldDefinition): FunSpec.Builder = apply {
        field.inputValueDefinitions.forEach { input ->
            val inputAnnotation = AnnotationSpec.builder(InputArgument::class)
                .addMember("\"${input.name}\"")
                .build()
            val inputType = ParameterSpec.builder(input.name, typeUtils.findReturnType(input.type))
                .addAnnotation(inputAnnotation)
                .build()
            addParameter(inputType)
        }
    }
}
