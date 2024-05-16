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

import com.netflix.graphql.dgs.codegen.*
import com.netflix.graphql.dgs.codegen.generators.shared.SiteTarget
import com.netflix.graphql.dgs.codegen.generators.shared.applyDirectivesJava
import com.squareup.javapoet.*
import graphql.language.*
import graphql.language.TypeName
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Serializable
import javax.lang.model.element.Modifier

class DataTypeGenerator(config: CodeGenConfig, document: Document) : BaseDataTypeGenerator(config.packageNameTypes, config, document) {
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
            val fieldsFromSuperTypes =
                document.getDefinitionsOfType(InterfaceTypeDefinition::class.java)
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
                .plus(
                    extensions
                        .flatMap { it.fieldDefinitions }
                        .filterSkipped()
                        .map { Field(it.name, typeUtils.findReturnType(it.type, useInterfaceType, true)) }
                )

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
                        typeUtils.findReturnType(it.type, useInterfaceType, true),
                        overrideGetter = overrideGetter,
                        description = it.description,
                        directives = it.directives
                    )
                }
                .plus(
                    extensions.flatMap { it.fieldDefinitions }.filterSkipped().map {
                        Field(
                            it.name,
                            typeUtils.findReturnType(it.type, useInterfaceType, true),
                            overrideGetter = overrideGetter,
                            description = it.description,
                            directives = it.directives
                        )
                    }
                )

            return generate(name, unionTypes + implements, fieldDefinitions, definition.description, definition.directives)
                .merge(interfaceCodeGenResult)
        }

        return interfaceCodeGenResult
    }
}

class InputTypeGenerator(config: CodeGenConfig, document: Document) : BaseDataTypeGenerator(config.packageNameTypes, config, document) {
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
                    is StringValue -> {
                        val localeValueOverride = checkAndGetLocaleValue(defVal, it.type)
                        if (localeValueOverride != null) CodeBlock.of("\$L", localeValueOverride)
                        else CodeBlock.of("\$S", defVal.value)
                    }
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
                    is ObjectValue -> CodeBlock.of("new \$L()", typeUtils.findReturnType(it.type))
                    else -> CodeBlock.of("\$L", defVal)
                }
            }
            Field(
                name = it.name,
                type = typeUtils.findReturnType(it.type),
                initialValue = defaultValue,
                description = it.description,
                directives = it.directives
            )
        }.plus(extensions.flatMap { it.inputValueDefinitions }.map { Field(it.name, typeUtils.findReturnType(it.type)) })
        return generate(name, emptyList(), fieldDefinitions, definition.description, definition.directives)
    }

    private fun checkAndGetLocaleValue(value: StringValue, type: Type<*>): String? {
        if (typeUtils.findReturnType(type).toString() == "java.util.Locale") return "Locale.forLanguageTag(\"${value.value}\")"
        return null
    }
}

internal data class Field(val name: String, val type: com.squareup.javapoet.TypeName, val initialValue: CodeBlock? = null, val overrideGetter: Boolean = false, val interfaceType: com.squareup.javapoet.TypeName? = null, val description: Description? = null, val directives: List<Directive> = listOf())

abstract class BaseDataTypeGenerator(
    internal val packageName: String,
    internal val config: CodeGenConfig,
    internal val document: Document
) {
    internal val typeUtils = TypeUtils(packageName, config, document)

    internal fun generate(
        name: String,
        interfaces: List<String>,
        fields: List<Field>,
        description: Description? = null,
        directives: List<Directive> = emptyList()
    ): CodeGenResult {
        val javaType = TypeSpec.classBuilder(name)
            .addOptionalGeneratedAnnotation(config)
            .addModifiers(Modifier.PUBLIC)

        if (config.implementSerializable) {
            javaType.addSuperinterface(ClassName.get(Serializable::class.java))
        }

        if (description != null) {
            javaType.addJavadoc(description.sanitizeJavaDoc())
        }

        if (directives.isNotEmpty()) {
            val (annotations, comments) = applyDirectivesJava(directives, config)
            if (annotations.containsKey(SiteTarget.DEFAULT.name)) {
                javaType.addAnnotations(annotations[SiteTarget.DEFAULT.name])
            }
            if (!comments.isNullOrBlank()) {
                javaType.addJavadoc("\$L", comments)
            }
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

        if (config.javaGenerateAllConstructor && fields.isNotEmpty()) {
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
            .addOptionalGeneratedAnnotation(config)
            .addModifiers(Modifier.PUBLIC)

        superInterfaces.forEach {
            javaType.addSuperinterface(typeUtils.findJavaInterfaceName((it as TypeName).name, packageName))
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
            val fieldValueStatement = if (field.directives.stream().anyMatch { it -> it.name.equals("sensitive") }) "\"*****\"" else ReservedKeywordSanitizer.sanitize(field.name)
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
            val parameterBuilder = ParameterSpec.builder(it.type, ReservedKeywordSanitizer.sanitize(it.name))
            if (it.directives.isNotEmpty()) {
                val (annotations, comments) = applyDirectivesJava(it.directives, config)
                annotations.forEach { entry ->
                    if (SiteTarget.valueOf(entry.key) == SiteTarget.PARAM) {
                        parameterBuilder.addAnnotations(annotations[SiteTarget.PARAM.name])
                    }
                }
            }
            constructorBuilder
                .addParameter(parameterBuilder.build())
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.\$N = \$N", ReservedKeywordSanitizer.sanitize(it.name), ReservedKeywordSanitizer.sanitize(it.name))
        }

        javaType.addMethod(constructorBuilder.build())
    }

    private fun addDefaultConstructor(javaType: TypeSpec.Builder) {
        javaType.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
    }

    private fun addInterface(type: String, javaType: TypeSpec.Builder) {
        val interfaceTypeMappedName: String? = config.typeMapping[type]
        val interfaceName: ClassName = if (interfaceTypeMappedName == null) ClassName.get(packageName, type) else ClassName.bestGuess(interfaceTypeMappedName)

        javaType.addSuperinterface(interfaceName)
    }

    private fun addField(fieldDefinition: Field, javaType: TypeSpec.Builder) {
        addFieldWithGetterAndSetter(fieldDefinition.type, fieldDefinition, javaType)
    }

    private fun addFieldWithGetterAndSetter(returnType: com.squareup.javapoet.TypeName?, fieldDefinition: Field, javaType: TypeSpec.Builder) {
        val fieldBuilder = if (fieldDefinition.initialValue != null) {
            FieldSpec
                .builder(fieldDefinition.type, ReservedKeywordSanitizer.sanitize(fieldDefinition.name))
                .addModifiers(Modifier.PRIVATE)
                .initializer(fieldDefinition.initialValue)
        } else {
            FieldSpec.builder(returnType, ReservedKeywordSanitizer.sanitize(fieldDefinition.name)).addModifiers(Modifier.PRIVATE)
        }

        if (fieldDefinition.description != null) {
            fieldBuilder.addJavadoc(fieldDefinition.description.sanitizeJavaDoc())
        }

        val getterPrefix = if (returnType == com.squareup.javapoet.TypeName.BOOLEAN && config.generateIsGetterForPrimitiveBooleanFields) "is" else "get"
        val getterName = typeUtils.transformIfDefaultClassMethodExists("${getterPrefix}${fieldDefinition.name[0].uppercase()}${fieldDefinition.name.substring(1)}", TypeUtils.Companion.getClass)

        val getterMethodBuilder = MethodSpec.methodBuilder(getterName).addModifiers(Modifier.PUBLIC).returns(returnType).addStatement("return \$N", ReservedKeywordSanitizer.sanitize(fieldDefinition.name))
        if (fieldDefinition.overrideGetter) {
            getterMethodBuilder.addAnnotation(Override::class.java)
        }

        if (fieldDefinition.description != null) {
            getterMethodBuilder.addJavadoc(fieldDefinition.description.sanitizeJavaDoc())
        }

        val setterName = typeUtils.transformIfDefaultClassMethodExists("set${fieldDefinition.name[0].uppercase()}${fieldDefinition.name.substring(1)}", TypeUtils.Companion.setClass)
        val parameterBuilder = ParameterSpec.builder(returnType, ReservedKeywordSanitizer.sanitize(fieldDefinition.name))
        val setterMethodBuilder = MethodSpec.methodBuilder(setterName)
            .addModifiers(Modifier.PUBLIC)
            .addStatement(
                "this.\$N = \$N",
                ReservedKeywordSanitizer.sanitize(fieldDefinition.name),
                ReservedKeywordSanitizer.sanitize(fieldDefinition.name)
            )

        if (fieldDefinition.directives.isNotEmpty()) {
            val (annotations, comments) = applyDirectivesJava(fieldDefinition.directives, config)
            if (!comments.isNullOrBlank()) {
                fieldBuilder.addJavadoc("\$L", comments)
            }
            for (entry in annotations) {
                when (SiteTarget.valueOf(entry.key)) {
                    SiteTarget.FIELD -> fieldBuilder.addAnnotations(annotations[SiteTarget.FIELD.name])
                    SiteTarget.GET -> getterMethodBuilder.addAnnotations(annotations[SiteTarget.GET.name])
                    SiteTarget.SET -> setterMethodBuilder.addAnnotations(annotations[SiteTarget.SET.name])
                    SiteTarget.SETPARAM -> parameterBuilder.addAnnotations(annotations[SiteTarget.SETPARAM.name])
                    SiteTarget.PARAM -> continue
                    else -> fieldBuilder.addAnnotations(annotations[entry.key])
                }
            }
        }
        setterMethodBuilder.addParameter(parameterBuilder.build())

        javaType.addField(fieldBuilder.build())
        javaType.addMethod(getterMethodBuilder.build())
        javaType.addMethod(setterMethodBuilder.build())
    }

    private fun addAbstractGetter(returnType: com.squareup.javapoet.TypeName?, fieldDefinition: Field, javaType: TypeSpec.Builder) {
        val getterPrefix = if (returnType == com.squareup.javapoet.TypeName.BOOLEAN && config.generateIsGetterForPrimitiveBooleanFields) "is" else "get"
        val getterName = "${getterPrefix}${fieldDefinition.name[0].uppercase()}${fieldDefinition.name.substring(1)}"
        javaType.addMethod(
            MethodSpec.methodBuilder(getterName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(returnType).build()
        )
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
        val newBuilderMethod =
            MethodSpec
                .methodBuilder("newBuilder")
                .returns(builderClassName)
                .addStatement("return new Builder()")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .build()

        javaType.addMethod(newBuilderMethod)

        val builderType =
            TypeSpec
                .classBuilder("Builder")
                .addOptionalGeneratedAnnotation(config)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(buildMethod)

        javaType.build().fieldSpecs.map {
            MethodSpec.methodBuilder(it.name)
                .addJavadoc(it.javadoc)
                .returns(builderClassName)
                .addStatement("this.${it.name} = ${it.name}")
                .addStatement("return this")
                .addParameter(ParameterSpec.builder(it.type, it.name).build())
                .addModifiers(Modifier.PUBLIC).build()
        }.forEach { builderType.addMethod(it) }

        builderType.addFields(javaType.build().fieldSpecs)
        javaType.addType(builderType.build())
    }
}
