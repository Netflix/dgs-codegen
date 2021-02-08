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
import com.netflix.graphql.dgs.codegen.filterSkipped
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.javapoet.*
import graphql.language.*
import graphql.language.TypeName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import javax.lang.model.element.Modifier

class DataTypeGenerator(config: CodeGenConfig) : BaseDataTypeGenerator(config.packageName + ".types", config) {
    fun generate(definition: ObjectTypeDefinition, extensions: List<ObjectTypeExtensionDefinition>, document: Document): CodeGenResult {
        if (definition.shouldSkip()) {
            return CodeGenResult()
        }

        val name = definition.name

        val unionTypes = document.getDefinitionsOfType(UnionTypeDefinition::class.java).filter { union ->
            union.memberTypes.map { it as TypeName }.map { it.name }.contains(name)
        }.map { it.name }

        val implements = definition.implements.filterIsInstance<TypeName>().map { typeUtils.findReturnType(it).toString() }
        val fieldDefinitions = definition.fieldDefinitions
                .filterSkipped()
                .map { Field(it.name, typeUtils.findReturnType(it.type)) }
                .plus(extensions.flatMap { it.fieldDefinitions }.filterSkipped().map { Field(it.name, typeUtils.findReturnType(it.type)) })

        return generate(name, unionTypes.plus(implements), fieldDefinitions, false)
    }
}

class InputTypeGenerator(config: CodeGenConfig) : BaseDataTypeGenerator(config.packageName + ".types", config) {
    fun generate(definition: InputObjectTypeDefinition, extensions: List<InputObjectTypeExtensionDefinition>): CodeGenResult {
        val name = definition.name

        val fieldDefinitions = definition.inputValueDefinitions.map {
            var defaultValue: Any
            if (it.defaultValue != null) {
                    defaultValue = when (it.defaultValue) {
                        is BooleanValue -> (it.defaultValue as BooleanValue).isValue
                        is IntValue -> (it.defaultValue as graphql.language.IntValue).value
                        is StringValue -> (it.defaultValue as graphql.language.StringValue).value
                        is FloatValue -> (it.defaultValue as graphql.language.FloatValue).value
                        else -> it.defaultValue
                    }
                Field(it.name, typeUtils.findReturnType(it.type), defaultValue)
            } else {
                Field(it.name, typeUtils.findReturnType(it.type))
            }

        }.plus(extensions.flatMap { it.inputValueDefinitions }.map { Field(it.name, typeUtils.findReturnType(it.type)) })
        return generate(name, emptyList(), fieldDefinitions, true)
    }
}

internal data class Field(val name: String, val type: com.squareup.javapoet.TypeName, val initialValue: Any? = null)

abstract class BaseDataTypeGenerator(internal val packageName: String, config: CodeGenConfig) {
    internal val typeUtils = TypeUtils(packageName, config)

    internal fun generate(name: String, interfaces: List<String>, fields: List<Field>, isInputType: Boolean): CodeGenResult {
        val javaType = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)

        interfaces.forEach {
            addInterface(it, javaType)
        }

        if (interfaces.isNotEmpty()) {
            javaType.addAnnotation(disableJsonTypeInfoAnnotation())
        }

        fields.forEach {
            addField(it, javaType)
        }

        addDefaultConstructor(javaType)

        if (fields.isNotEmpty()) {
            addParameterizedConstructor(fields, javaType)
        }

        if (isInputType) {
            addToInputString(fields, javaType)
        } else {
            addToString(fields, javaType)
        }

        addEquals(javaType)
        addHashcode(javaType)
        addBuilder(javaType)

        val javaFile = JavaFile.builder(packageName, javaType.build()).build()

        return CodeGenResult(dataTypes = listOf(javaFile))
    }

    private fun addHashcode(javaType: TypeSpec.Builder) {
        val methodBuilder = MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .returns(com.squareup.javapoet.TypeName.INT)

        val fieldSpecs = javaType.build().fieldSpecs
        methodBuilder.addStatement("return java.util.Objects.hash(${fieldSpecs.joinToString(", ") { it.name }})")
        javaType.addMethod(methodBuilder.build())
    }

    private fun addEquals(javaType: TypeSpec.Builder) {
        val methodBuilder = MethodSpec.methodBuilder("equals")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .returns(com.squareup.javapoet.TypeName.BOOLEAN)
                .addParameter(ClassName.get(Object::class.java), "o")

        val equalsBody = StringBuilder("""
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
             ${javaType.build().name} that = (${javaType.build().name}) o;
             return 
        """.trimIndent())

        val fieldSpecs = javaType.build().fieldSpecs
        fieldSpecs.forEachIndexed { index, fieldSpec ->
            if (fieldSpec.type.isPrimitive) {
                equalsBody.append("${fieldSpec.name} == that.${fieldSpec.name}")
            } else {
                equalsBody.append("java.util.Objects.equals(${fieldSpec.name}, that.${fieldSpec.name})")
            }

            if (index != fieldSpecs.size - 1) {
                equalsBody.append(""" &&
                    
                """.trimMargin())
            }
        }

        if (fieldSpecs.size == 0) {
            equalsBody.append("false")
        }

        methodBuilder.addStatement(equalsBody.toString())
        javaType.addMethod(methodBuilder.build())
    }

    private fun addToString(fieldDefinitions: List<Field>, javaType: TypeSpec.Builder) {
        val methodBuilder = MethodSpec.methodBuilder("toString").addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC).returns(String::class.java)
        val toStringBody = StringBuilder("return \"${javaType.build().name}{\" + ")
        fieldDefinitions.forEachIndexed { index, field ->
            toStringBody.append("""
                "${field.name}='" + ${ReservedKeywordSanitizer.sanitize(field.name)} + "'${if (index < fieldDefinitions.size - 1) "," else ""}" +
            """.trimIndent())
        }

        toStringBody.append("""
            "}"
        """.trimIndent())

        methodBuilder.addStatement(toStringBody.toString())
        javaType.addMethod(methodBuilder.build())
    }

    private fun addToInputString(fieldDefinitions: List<Field>, javaType: TypeSpec.Builder) {
        val methodBuilder = MethodSpec.methodBuilder("toString").addModifiers(Modifier.PUBLIC).returns(String::class.java)
        val toStringBody = StringBuilder("return \"{\" + ")
        val fieldSpecs = javaType.build().fieldSpecs
        fieldSpecs.mapIndexed { index, fieldSpec ->
            when (val fieldSpecType = fieldSpec.type) {
                is ParameterizedTypeName -> {
                    if (typeUtils.isStringInput(fieldSpecType.typeArguments[0])) {
                            val name = "serializeListOf" + (fieldSpecType.typeArguments[0] as ClassName).simpleName()
                            addToStringForListOfStrings(name, fieldSpec, javaType)
                            """
                            "${fieldSpec.name}:" + ${name}(${fieldSpec.name}) + "${if (index < fieldDefinitions.size - 1) "," else ""}" +
                            """.trimIndent()
                    } else {
                        defaultString(fieldSpec, index, fieldDefinitions)
                    }
                }
                is ClassName -> {
                    if (typeUtils.isStringInput(fieldSpecType)) {
                        quotedString(fieldSpec, index, fieldDefinitions)
                    } else {
                        defaultString(fieldSpec, index, fieldDefinitions)
                    }
                }
                else -> defaultString(fieldSpec, index, fieldDefinitions)

            }
        }.forEach { toStringBody.append(it)}

        toStringBody.append("""
            "}"
        """.trimIndent())

        methodBuilder.addStatement(toStringBody.toString())
        javaType.addMethod(methodBuilder.build())
    }

    private fun defaultString(fieldSpec: FieldSpec, index: Int, fieldDefinitions: List<Field>): String {
        return """
            "${fieldSpec.name}:" + ${fieldSpec.name} + "${if (index < fieldDefinitions.size - 1) "," else ""}" +
            """.trimIndent()
    }

    private fun quotedString(fieldSpec: FieldSpec, index: Int, fieldDefinitions: List<Field>): String {
        return """
            "${fieldSpec.name}:" + (${fieldSpec.name} != null?"\"":"") + ${fieldSpec.name} + (${fieldSpec.name} != null?"\"":"") + "${if (index < fieldDefinitions.size - 1) "," else ""}" +
            """.trimIndent()
    }

    private fun addToStringForListOfStrings(name: String, field: FieldSpec, javaType: TypeSpec.Builder) {
        if(javaType.methodSpecs.any { it.name == name }) return

        val methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(field.type, "inputList")
                .returns(String::class.java)

        val toStringBody = StringBuilder("""
                if (inputList == null) {
                    return null;
                }
                StringBuilder builder = new java.lang.StringBuilder();
                builder.append("[");
            
                if (! inputList.isEmpty()) {
                    String result = inputList.stream()
                            .map( iter -> iter.toString() )
                            .collect(java.util.stream.Collectors.joining("\", \"", "\"", "\""));
                    builder.append(result);
                }
                builder.append("]");
                return  builder.toString()
        """.trimIndent())

        methodBuilder.addStatement(toStringBody.toString())
        javaType.addMethod(methodBuilder.build())
    }

    private fun addParameterizedConstructor(fieldDefinitions: List<Field>, javaType: TypeSpec.Builder) {

        val constructorBuilder = MethodSpec.constructorBuilder()
        fieldDefinitions.forEach {
            constructorBuilder
                    .addParameter(it.type, ReservedKeywordSanitizer.sanitize(it.name))
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("this.\$N = \$N", ReservedKeywordSanitizer.sanitize(it.name), ReservedKeywordSanitizer.sanitize(it.name))
        }

        javaType.addMethod(constructorBuilder.build())
    }

    private fun addDefaultConstructor(javaType: TypeSpec.Builder) {
        javaType.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
    }

    private fun addInterface(type: String, javaType: TypeSpec.Builder) {
        javaType.addSuperinterface(ClassName.get(packageName, type))
    }

    private fun addField(fieldDefinition: Field, javaType: TypeSpec.Builder) {
        addFieldWithGetterAndSetter(fieldDefinition.type, fieldDefinition, javaType)
    }

    private fun addFieldWithGetterAndSetter(returnType: com.squareup.javapoet.TypeName?, fieldDefinition: Field, javaType: TypeSpec.Builder) {
        if (fieldDefinition.initialValue != null) {
            var initializerBlock = if (fieldDefinition.type.toString().contains("String")) {
                "\"${fieldDefinition.initialValue}\""
            } else {
                "${fieldDefinition.initialValue}"
            }
            val field = FieldSpec.builder(fieldDefinition.type, fieldDefinition.name).addModifiers(Modifier.PRIVATE)
                .initializer(initializerBlock)
                .build()
            javaType.addField(field)
        } else {
            val field = FieldSpec.builder(returnType, ReservedKeywordSanitizer.sanitize(fieldDefinition.name)).addModifiers(Modifier.PRIVATE).build()
            javaType.addField(field)
        }

        val getterName = "get${fieldDefinition.name[0].toUpperCase()}${fieldDefinition.name.substring(1)}"
        javaType.addMethod(MethodSpec.methodBuilder(getterName).addModifiers(Modifier.PUBLIC).returns(returnType).addStatement("return \$N", ReservedKeywordSanitizer.sanitize(fieldDefinition.name)).build())

        val setterName = "set${fieldDefinition.name[0].toUpperCase()}${fieldDefinition.name.substring(1)}"
        javaType.addMethod(MethodSpec.methodBuilder(setterName).addModifiers(Modifier.PUBLIC).addParameter(returnType, ReservedKeywordSanitizer.sanitize(fieldDefinition.name)).addStatement("this.\$N = \$N", ReservedKeywordSanitizer.sanitize(fieldDefinition.name), ReservedKeywordSanitizer.sanitize(fieldDefinition.name)).build())
    }

    private fun addBuilder(javaType: TypeSpec.Builder) {

        val className = ClassName.get(packageName, javaType.build().name)
        val buildMethod = MethodSpec.methodBuilder("build").returns(className).addStatement("""
            ${className} result = new ${className}();
            ${javaType.build().fieldSpecs.joinToString("\n") { "result.${it.name} = this.${it.name};" }}
            return result
        """.trimIndent()).addModifiers(Modifier.PUBLIC).build()

        val builderClassName = ClassName.get(packageName, "${className}.Builder")
        val newBuilderMethod = MethodSpec.methodBuilder("newBuilder").returns(builderClassName).addStatement("return new Builder()").addModifiers(Modifier.PUBLIC, Modifier.STATIC).build()
        javaType.addMethod(newBuilderMethod)

        val builderType = TypeSpec.classBuilder("Builder").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(buildMethod)

        javaType.build().fieldSpecs.map { MethodSpec.methodBuilder(it.name).returns(builderClassName).addStatement("this.${it.name} = ${it.name}").addStatement("return this").addParameter(ParameterSpec.builder(it.type, it.name).build()).addModifiers(Modifier.PUBLIC).build() }.forEach { builderType.addMethod(it) }
        builderType.addFields(javaType.build().fieldSpecs)
        javaType.addType(builderType.build())
    }


}