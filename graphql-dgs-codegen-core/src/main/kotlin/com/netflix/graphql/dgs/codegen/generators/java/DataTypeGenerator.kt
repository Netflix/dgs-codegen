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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.lang.model.element.Modifier

class DataTypeGenerator(private val config: CodeGenConfig, private val document: Document) : BaseDataTypeGenerator(config.packageNameTypes, config, document) {
    private val logger: Logger = LoggerFactory.getLogger(DataTypeGenerator::class.java)

    fun generate(definition: ObjectTypeDefinition, extensions: List<ObjectTypeExtensionDefinition>): CodeGenResult {
        if (definition.shouldSkip(config)) {
            return CodeGenResult()
        }

        logger.info("Generating data type ${definition.name}")

        val name = definition.name
        val unionTypes = document.getDefinitionsOfType(UnionTypeDefinition::class.java).filter { union ->
            union.memberTypes.asSequence().map { it as TypeName }.any { it.name == name }
        }.map { it.name }

        var implements = definition.implements.filterIsInstance<TypeName>().map { typeUtils.findReturnType(it).toString() }

        var useInterfaceType = false
        var overrideGetter = false
        var interfaceCodeGenResult = CodeGenResult()

        if (config.generateInterfaces) {
            useInterfaceType = true
            val fieldsFromSuperTypes = document.getDefinitionsOfType(InterfaceTypeDefinition::class.java)
                .filter { ClassName.get(packageName, it.name).toString() in implements }
                .flatMap { it.fieldDefinitions }
                .map { it.name }

            overrideGetter = true
            val fieldDefinitions = definition.fieldDefinitions
                .filterSkipped()
                .filter { it.name !in fieldsFromSuperTypes }
                .map {
                    Field(it.name, typeUtils.findReturnType(it.type, useInterfaceType, true))
                }
                .plus(extensions.flatMap { it.fieldDefinitions }.filterSkipped().map { Field(it.name, typeUtils.findReturnType(it.type, useInterfaceType, true)) })
            val interfaceName = "I$name"
            implements = listOf(interfaceName) + implements
            val superInterfaces = definition.implements
            interfaceCodeGenResult = generateInterface(interfaceName, superInterfaces, fieldDefinitions)
        }

        if (config.generateDataTypes) {
            val fieldDefinitions = definition.fieldDefinitions
                .filterSkipped()
                .map {
                    Field(
                        it.name,
                        typeUtils.findReturnType(it.type, useInterfaceType),
                        overrideGetter = overrideGetter,
                        description = it.description,
                        directives = it.directives.map { directive -> directive.name }
                    )
                }
                .plus(
                    extensions.flatMap { it.fieldDefinitions }.filterSkipped().map {
                        Field(
                            it.name,
                            typeUtils.findReturnType(it.type, useInterfaceType),
                            overrideGetter = overrideGetter,
                            description = it.description,
                            directives = it.directives.map { directive -> directive.name }
                        )
                    }
                )

            return generate(name, unionTypes + implements, fieldDefinitions, definition.description, config.generateAllConstructor)
                .merge(interfaceCodeGenResult)
        }

        return interfaceCodeGenResult
    }
}

class InputTypeGenerator(private val config: CodeGenConfig, document: Document) : BaseDataTypeGenerator(config.packageNameTypes, config, document) {
    private val logger: Logger = LoggerFactory.getLogger(InputTypeGenerator::class.java)

    fun generate(definition: InputObjectTypeDefinition, extensions: List<InputObjectTypeExtensionDefinition>): CodeGenResult {
        if (definition.shouldSkip(config)) {
            return CodeGenResult()
        }

        logger.info("Generating input type ${definition.name}")

        val name = definition.name
        val fieldDefinitions = definition.inputValueDefinitions.map {
            val defaultValue = it.defaultValue?.let { defVal ->
                when (defVal) {
                    is BooleanValue -> CodeBlock.of("\$L", defVal.isValue)
                    is IntValue -> CodeBlock.of("\$L", defVal.value)
                    is StringValue -> CodeBlock.of("\$S", defVal.value)
                    is FloatValue -> CodeBlock.of("\$L", defVal.value)
                    is EnumValue -> CodeBlock.of("\$T.\$N", typeUtils.findReturnType(it.type), defVal.name)
                    is ArrayValue -> if (defVal.values.isEmpty()) CodeBlock.of("java.util.Collections.emptyList()") else CodeBlock.of(
                        "java.util.Arrays.asList(\$L)",
                        defVal.values.map { v ->
                            when (v) {
                                is BooleanValue -> CodeBlock.of("\$L", v.isValue)
                                is IntValue -> CodeBlock.of("\$L", v.value)
                                is StringValue -> CodeBlock.of("\$S", v.value)
                                is FloatValue -> CodeBlock.of("\$L", v.value)
                                is EnumValue -> CodeBlock.of("\$L.\$N", ((it.type as ListType).type as TypeName).name, v.name)
                                else -> ""
                            }
                        }.joinToString()
                    )
                    else -> CodeBlock.of("\$L", defVal)
                }
            }
            Field(name = it.name, type = typeUtils.findReturnType(it.type), initialValue = defaultValue, description = it.description, directives = it.directives.map { directive -> directive.name })
        }.plus(extensions.flatMap { it.inputValueDefinitions }.map { Field(it.name, typeUtils.findReturnType(it.type)) })
        return generate(name, emptyList(), fieldDefinitions, definition.description, config.generateAllConstructor)
    }
}

internal data class Field(val name: String, val type: com.squareup.javapoet.TypeName, val initialValue: CodeBlock? = null, val overrideGetter: Boolean = false, val interfaceType: com.squareup.javapoet.TypeName? = null, val description: Description? = null, val directives: List<String> = listOf<String>())

abstract class BaseDataTypeGenerator(internal val packageName: String, config: CodeGenConfig, document: Document) {
    internal val typeUtils = TypeUtils(packageName, config, document)

    internal fun generate(
        name: String,
        interfaces: List<String>,
        fields: List<Field>,
        description: Description? = null,
        generateAllConstructor: Boolean,
    ): CodeGenResult {
        val javaType = TypeSpec.classBuilder(name)
            .addModifiers(Modifier.PUBLIC)

        if (description != null) {
            javaType.addJavadoc(description.sanitizeJavaDoc())
        }

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

        if (generateAllConstructor && fields.isNotEmpty()) {
            addParameterizedConstructor(fields, javaType)
        }

        addToString(fields, javaType)

        addEquals(javaType)
        addHashcode(javaType)
        addBuilder(javaType)

        val javaFile = JavaFile.builder(packageName, javaType.build()).build()

        return CodeGenResult(javaDataTypes = listOf(javaFile))
    }

    internal fun generateInterface(name: String, superInterfaces: List<Type<*>>, fields: List<Field>): CodeGenResult {
        val javaType = TypeSpec.interfaceBuilder(name)
            .addModifiers(Modifier.PUBLIC)

        superInterfaces.forEach {
            javaType.addSuperinterface(ClassName.get(packageName, (it as TypeName).name))
        }

        fields.forEach {
            addAbstractGetter(it.type, it, javaType)
        }

        val javaFile = JavaFile.builder(packageName, javaType.build()).build()

        return CodeGenResult(javaInterfaces = listOf(javaFile))
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

        val equalsBody = StringBuilder(
            """
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
             ${javaType.build().name} that = (${javaType.build().name}) o;
             return 
            """.trimIndent()
        )

        val fieldSpecs = javaType.build().fieldSpecs
        fieldSpecs.forEachIndexed { index, fieldSpec ->
            if (fieldSpec.type.isPrimitive) {
                equalsBody.append("${fieldSpec.name} == that.${fieldSpec.name}")
            } else {
                equalsBody.append("java.util.Objects.equals(${fieldSpec.name}, that.${fieldSpec.name})")
            }

            if (index != fieldSpecs.size - 1) {
                equalsBody.append(
                    """ &&
                    
                """.trimMargin()
                )
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
            val fieldValueStatement = if (field.directives.contains("sensitive")) "\"*****\"" else ReservedKeywordSanitizer.sanitize(field.name)
            toStringBody.append(
                """
                "${field.name}='" + $fieldValueStatement + "'${if (index < fieldDefinitions.size - 1) "," else ""}" +
                """.trimIndent()
            )
        }

        toStringBody.append(
            """
            "}"
            """.trimIndent()
        )

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
        val fieldBuilder = if (fieldDefinition.initialValue != null) {
            FieldSpec.builder(fieldDefinition.type, ReservedKeywordSanitizer.sanitize(fieldDefinition.name)).addModifiers(Modifier.PRIVATE).initializer(fieldDefinition.initialValue)
        } else {
            FieldSpec.builder(returnType, ReservedKeywordSanitizer.sanitize(fieldDefinition.name)).addModifiers(Modifier.PRIVATE)
        }

        if (fieldDefinition.description != null) {
            fieldBuilder.addJavadoc(fieldDefinition.description.sanitizeJavaDoc())
        }

        val field = fieldBuilder.build()

        javaType.addField(field)

        val getterName = "get${fieldDefinition.name[0].uppercase()}${fieldDefinition.name.substring(1)}"
        val getterMethodBuilder = MethodSpec.methodBuilder(getterName).addModifiers(Modifier.PUBLIC).returns(returnType).addStatement("return \$N", ReservedKeywordSanitizer.sanitize(fieldDefinition.name))
        if (fieldDefinition.overrideGetter) {
            getterMethodBuilder.addAnnotation(Override::class.java)
        }

        if (fieldDefinition.description != null) {
            getterMethodBuilder.addJavadoc(fieldDefinition.description.sanitizeJavaDoc())
        }

        javaType.addMethod(getterMethodBuilder.build())

        val setterName = "set${fieldDefinition.name[0].uppercase()}${fieldDefinition.name.substring(1)}"
        javaType.addMethod(
            MethodSpec.methodBuilder(setterName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(returnType, ReservedKeywordSanitizer.sanitize(fieldDefinition.name))
                .addStatement(
                    "this.\$N = \$N", ReservedKeywordSanitizer.sanitize(fieldDefinition.name),
                    ReservedKeywordSanitizer.sanitize(fieldDefinition.name)
                ).build()
        )
    }

    private fun addAbstractGetter(returnType: com.squareup.javapoet.TypeName?, fieldDefinition: Field, javaType: TypeSpec.Builder) {
        val getterName = "get${fieldDefinition.name[0].uppercase()}${fieldDefinition.name.substring(1)}"
        javaType.addMethod(MethodSpec.methodBuilder(getterName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).returns(returnType).build())
    }

    private fun addBuilder(javaType: TypeSpec.Builder) {

        val className = ClassName.get(packageName, javaType.build().name)
        val buildMethod = MethodSpec.methodBuilder("build").returns(className).addStatement(
            """
            $className result = new $className();
            ${javaType.build().fieldSpecs.joinToString("\n") { "result.${it.name} = this.${it.name};" }}
            return result
            """.trimIndent()
        ).addModifiers(Modifier.PUBLIC).build()

        val builderClassName = ClassName.get(packageName, "$className.Builder")
        val newBuilderMethod = MethodSpec.methodBuilder("newBuilder").returns(builderClassName).addStatement("return new Builder()").addModifiers(Modifier.PUBLIC, Modifier.STATIC).build()
        javaType.addMethod(newBuilderMethod)

        val builderType = TypeSpec.classBuilder("Builder").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addMethod(buildMethod)

        javaType.build().fieldSpecs.map {
            MethodSpec.methodBuilder(it.name)
                .addJavadoc(it.javadoc)
                .returns(builderClassName)
                .addStatement("this.${it.name} = ${it.name}")
                .addStatement("return this")
                .addParameter(ParameterSpec.builder(it.type, it.name).build())
                .addModifiers(Modifier.PUBLIC).build()
        }
            .forEach { builderType.addMethod(it) }
        builderType.addFields(javaType.build().fieldSpecs)
        javaType.addType(builderType.build())
    }
}
