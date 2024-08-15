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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netflix.graphql.dgs.codegen.*
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.netflix.graphql.dgs.codegen.generators.shared.SiteTarget
import com.netflix.graphql.dgs.codegen.generators.shared.applyDirectivesJava
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import graphql.language.ArrayValue
import graphql.language.BooleanValue
import graphql.language.Description
import graphql.language.Directive
import graphql.language.Document
import graphql.language.EnumValue
import graphql.language.FloatValue
import graphql.language.InputObjectTypeDefinition
import graphql.language.InputObjectTypeExtensionDefinition
import graphql.language.IntValue
import graphql.language.InterfaceTypeDefinition
import graphql.language.ObjectTypeDefinition
import graphql.language.ObjectTypeExtensionDefinition
import graphql.language.ObjectValue
import graphql.language.StringValue
import graphql.language.Type
import graphql.language.TypeName
import graphql.language.UnionTypeDefinition
import graphql.language.Value
import graphql.schema.idl.TypeUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.math.BigDecimal
import java.util.Arrays
import java.util.Collections
import java.util.Locale
import java.util.Objects
import javax.lang.model.element.Modifier
import com.squareup.javapoet.TypeName as JavaTypeName

class DataTypeGenerator(config: CodeGenConfig, document: Document) : BaseDataTypeGenerator(config.packageNameTypes, config, document) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DataTypeGenerator::class.java)
    }

    fun generate(definition: ObjectTypeDefinition, extensions: List<ObjectTypeExtensionDefinition>): CodeGenResult {
        if (definition.shouldSkip(config)) {
            return CodeGenResult.EMPTY
        }

        logger.info("Generating data type {}", definition.name)

        val name = definition.name
        val unionTypes = document.getDefinitionsOfType(UnionTypeDefinition::class.java).asSequence().filter { union ->
            union.memberTypes.asSequence().map { it as TypeName }.any { it.name == name }
        }.map { it.name }.toList()

        var implements = (definition.implements + extensions.flatMap { it.implements })
            .asSequence().filterIsInstance<TypeName>().map { typeUtils.findReturnType(it).toString() }.toList()

        var useInterfaceType = false
        var overrideGetter = false
        var interfaceCodeGenResult = CodeGenResult.EMPTY

        if (config.generateInterfaces) {
            useInterfaceType = true
            val fieldsFromSuperTypes =
                document.getDefinitionsOfType(InterfaceTypeDefinition::class.java)
                    .asSequence()
                    .filter { ClassName.get(packageName, it.name).toString() in implements }
                    .flatMap { it.fieldDefinitions }
                    .map { it.name }

            overrideGetter = true
            val fieldDefinitions = definition.fieldDefinitions
                .asSequence()
                .filterSkipped()
                .filter { it.name !in fieldsFromSuperTypes }
                .map {
                    Field(it.name, typeUtils.findReturnType(it.type, useInterfaceType, true))
                }
                .plus(
                    extensions
                        .asSequence()
                        .flatMap { it.fieldDefinitions }
                        .filterSkipped()
                        .map { Field(it.name, typeUtils.findReturnType(it.type, useInterfaceType, true)) }
                )
                .toList()

            val interfaceName = "I$name"
            implements = listOf(interfaceName) + implements
            val superInterfaces = definition.implements + extensions.flatMap { it.implements }
            interfaceCodeGenResult = generateInterface(interfaceName, superInterfaces, fieldDefinitions)
        }

        if (config.generateDataTypes) {
            val fieldDefinitions = definition.fieldDefinitions
                .asSequence()
                .filterSkipped()
                .map { fieldDefinition ->
                    val isNullable = !TypeUtil.isNonNull(fieldDefinition.type)
                    Field(
                        fieldDefinition.name,
                        typeUtils.findReturnType(fieldDefinition.type, useInterfaceType, true),
                        overrideGetter = overrideGetter,
                        description = fieldDefinition.description,
                        directives = fieldDefinition.directives,
                        isNullable = isNullable
                    )
                }
                .plus(
                    extensions.asSequence().flatMap { it.fieldDefinitions }.filterSkipped().map {
                        Field(
                            it.name,
                            typeUtils.findReturnType(it.type, useInterfaceType, true),
                            overrideGetter = overrideGetter,
                            description = it.description,
                            directives = it.directives
                        )
                    }
                )
                .toList()

            return generate(name, unionTypes + implements, fieldDefinitions, definition.description, definition.directives)
                .merge(interfaceCodeGenResult)
        }

        return interfaceCodeGenResult
    }
}

class InputTypeGenerator(config: CodeGenConfig, document: Document) : BaseDataTypeGenerator(config.packageNameTypes, config, document) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(InputTypeGenerator::class.java)
        private val LOCALE: ClassName = ClassName.get(Locale::class.java)
        private val BIG_DECIMAL: ClassName = ClassName.get(BigDecimal::class.java)
    }

    fun generate(
        definition: InputObjectTypeDefinition,
        extensions: List<InputObjectTypeExtensionDefinition>,
        inputTypeDefinitions: List<InputObjectTypeDefinition>
    ): CodeGenResult {
        if (definition.shouldSkip(config)) {
            return CodeGenResult.EMPTY
        }

        logger.info("Generating input type {}", definition.name)

        val name = definition.name
        val fieldDefinitions = definition.inputValueDefinitions.asSequence().map {
            val isNullable = !TypeUtil.isNonNull(it.type)
            val type = typeUtils.findReturnType(it.type)
            val defaultValue = it.defaultValue?.let { defVal ->
                generateCode(defVal, type, inputTypeDefinitions)
            }
            Field(
                name = it.name,
                type = type,
                initialValue = defaultValue,
                description = it.description,
                directives = it.directives,
                isNullable = isNullable
            )
        }.plus(extensions.asSequence().flatMap { it.inputValueDefinitions }.map { Field(it.name, typeUtils.findReturnType(it.type)) })
            .toList()
        return generate(name, emptyList(), fieldDefinitions, definition.description, definition.directives, true)
    }

    private fun generateCode(
        value: Value<out Value<*>>,
        type: JavaTypeName,
        inputTypeDefinitions: List<InputObjectTypeDefinition>
    ): CodeBlock {
        if (type == LOCALE) {
            return localeCodeBlock(value, type)
        } else if (type == BIG_DECIMAL) {
            return bigDecimalCodeBlock(value, type)
        }
        return when (value) {
            is BooleanValue -> CodeBlock.of("\$L", value.isValue)
            is IntValue -> CodeBlock.of("\$L", value.value)
            is StringValue -> CodeBlock.of("\$S", value.value)
            is FloatValue -> CodeBlock.of("\$L", value.value)
            is EnumValue -> CodeBlock.of("\$T.\$N", type, value.name)
            is ArrayValue -> if (value.values.isEmpty()) {
                CodeBlock.of("\$T.emptyList()", Collections::class.java)
            } else {
                CodeBlock.of(
                    "\$T.asList(\$L)",
                    Arrays::class.java,
                    CodeBlock.join(value.values.map { generateCode(it, type.className, inputTypeDefinitions) }, ", ")
                )
            }
            is ObjectValue -> {
                val inputObjectDefinition = inputTypeDefinitions.first {
                    val expectedCanonicalClassName = config.typeMapping[it.name] ?: "${config.packageNameTypes}.${it.name}"
                    expectedCanonicalClassName == type.className.canonicalName()
                }
                if (value.objectFields.isEmpty()) {
                    return CodeBlock.of("new \$T()", type)
                } else {
                    CodeBlock.of(
                        "new \$T(){{\$L}}",
                        type,
                        CodeBlock.join(
                            value.objectFields.map { objectProperty ->
                                val argumentType = inputObjectDefinition.inputValueDefinitions.find { it.name == objectProperty.name }
                                    ?: error("""Property "${objectProperty.name}" does not exist in input type "${inputObjectDefinition.name}"""")
                                val argumentValue = generateCode(
                                    objectProperty.value,
                                    typeUtils.findReturnType(argumentType.type),
                                    inputTypeDefinitions
                                )
                                CodeBlock.of("set\$L(\$L);", objectProperty.name.replaceFirstChar { it.uppercaseChar() }, argumentValue)
                            },
                            ""
                        )
                    )
                }
            }
            else -> CodeBlock.of("\$L", value)
        }
    }

    private fun localeCodeBlock(value: Value<out Value<*>>, type: JavaTypeName): CodeBlock {
        check(value is StringValue) { "$type cannot be created from $value, expected String value" }
        return CodeBlock.of("\$T.forLanguageTag(\$S)", LOCALE, value.value)
    }

    private fun bigDecimalCodeBlock(value: Value<out Value<*>>, type: JavaTypeName): CodeBlock {
        return when (value) {
            is StringValue -> CodeBlock.of("new \$T(\$S)", BIG_DECIMAL, value.value)
            is IntValue -> CodeBlock.of("new \$T(\$L)", BIG_DECIMAL, value.value)
            is FloatValue -> CodeBlock.of("new \$T(\$L)", BIG_DECIMAL, value.value)
            else -> error("$type cannot be created from $value, expected String, Int or Float value")
        }
    }

    private val JavaTypeName.className: ClassName
        get() = when (this) {
            is ClassName -> this
            is ParameterizedTypeName -> typeArguments.first().className
            else -> throw UnsupportedOperationException("Unknown type: ${this.javaClass}")
        }
}

internal data class Field(val name: String, val type: JavaTypeName, val initialValue: CodeBlock? = null, val overrideGetter: Boolean = false, val interfaceType: com.squareup.javapoet.TypeName? = null, val description: Description? = null, val directives: List<Directive> = listOf(), val isNullable: Boolean = true)

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
        directives: List<Directive> = emptyList(),
        isInputType: Boolean = false
    ): CodeGenResult {
        val javaType = TypeSpec.classBuilder(name)
            .addOptionalGeneratedAnnotation(config)
            .addModifiers(Modifier.PUBLIC)

        if (config.implementSerializable) {
            javaType.addSuperinterface(Serializable::class.java)
        }

        if (description != null) {
            javaType.addJavadoc("\$L", description.content)
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
            addField(it, javaType, isInputType)
        }

        addDefaultConstructor(javaType)

        if (config.javaGenerateAllConstructor && fields.isNotEmpty()) {
            addParameterizedConstructor(fields, javaType, isInputType)
        }

        addToString(fields, javaType)

        addEquals(fields, javaType)
        addHashcode(javaType)
        addBuilder(fields, javaType, isInputType)

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
        val builtType = javaType.build()
        if (builtType.fieldSpecs.isEmpty()) {
            return
        }
        val methodBuilder = MethodSpec.methodBuilder("hashCode")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(JavaTypeName.INT)

        methodBuilder.addStatement(
            "return \$T.hash(\$L)",
            Objects::class.java,
            builtType.fieldSpecs.joinToString(", ") { it.name }
        )
        javaType.addMethod(methodBuilder.build())
    }

    private fun addEquals(fields: List<Field>, javaType: TypeSpec.Builder) {
        val builtType = javaType.build()
        if (builtType.fieldSpecs.isEmpty()) {
            return
        }

        val methodBuilder = MethodSpec.methodBuilder("equals")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(JavaTypeName.BOOLEAN)
            .addParameter(JavaTypeName.OBJECT, "o")

        methodBuilder.addStatement("if (this == o) return true")
        methodBuilder.addStatement("if (o == null || getClass() != o.getClass()) return false")
        methodBuilder.addStatement("\$L that = (\$L) o", builtType.name, builtType.name)

        methodBuilder.addStatement(
            "return \$L",
            CodeBlock.join(
                builtType.fieldSpecs
                    // Skip generated Boolean presence fields
                    .filterNot { fieldSpec -> fields.any { field -> generateBooleanFieldName(ReservedKeywordSanitizer.sanitize(field.name)) == fieldSpec.name } }
                    .map { field ->
                        if (field.type.isPrimitive) {
                            CodeBlock.of("\$L == that.\$L", field.name, field.name)
                        } else {
                            CodeBlock.of("\$T.equals(\$L, that.\$L)", Objects::class.java, field.name, field.name)
                        }
                    },
                " &&\n"
            )
        )
        javaType.addMethod(methodBuilder.build())
    }

    private fun addToString(fieldDefinitions: List<Field>, javaType: TypeSpec.Builder) {
        val builtType = javaType.build()
        val methodBuilder = MethodSpec.methodBuilder("toString")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(String::class.java)

        val toStringBody = CodeBlock.builder()
            .add("return \"\$L{", builtType.name)
        for ((idx, fieldDef) in fieldDefinitions.withIndex()) {
            if (fieldDef.directives.any { it.name == "sensitive" }) {
                toStringBody.add("\$L='*****'", fieldDef.name)
            } else {
                toStringBody.add("\$L='\" + \$L + \"'", fieldDef.name, ReservedKeywordSanitizer.sanitize(fieldDef.name))
            }
            if (idx != fieldDefinitions.lastIndex) {
                toStringBody.add(", ")
            }
        }
        toStringBody.add("}\";")

        methodBuilder.addCode(toStringBody.build())
        javaType.addMethod(methodBuilder.build())
    }

    private fun addParameterizedConstructor(fieldDefinitions: List<Field>, javaType: TypeSpec.Builder, isInputType: Boolean = false) {
        val constructorBuilder = MethodSpec.constructorBuilder()
        for (fieldDefinition in fieldDefinitions) {
            val sanitizedName = ReservedKeywordSanitizer.sanitize(fieldDefinition.name)
            val parameterBuilder = ParameterSpec.builder(fieldDefinition.type, sanitizedName)
            if (fieldDefinition.directives.isNotEmpty()) {
                val (annotations, _) = applyDirectivesJava(fieldDefinition.directives, config)
                val parameterAnnotations = annotations[SiteTarget.PARAM.name]
                if (parameterAnnotations != null) {
                    parameterBuilder.addAnnotations(parameterAnnotations)
                }
            }
            constructorBuilder
                .addParameter(parameterBuilder.build())
                .addModifiers(Modifier.PUBLIC)
        }

        fieldDefinitions.forEach {
            val constructor = constructorBuilder
                .addStatement(
                    "this.\$N = \$N",
                    ReservedKeywordSanitizer.sanitize(it.name),
                    ReservedKeywordSanitizer.sanitize(it.name)
                )
            if (config.generateIsSetFields && isInputType && it.isNullable && it.initialValue == null) {
                constructorBuilder
                    .addStatement(
                        "this.\$N = true",
                        generateBooleanFieldName(ReservedKeywordSanitizer.sanitize(it.name))
                    )
            }
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

    private fun addField(fieldDefinition: Field, javaType: TypeSpec.Builder, isInputType: Boolean = false) {
        addFieldWithGetterAndSetter(fieldDefinition.type, fieldDefinition, javaType, isInputType)
        // Generate for all nullable fields without any defaults
        if (config.generateIsSetFields && isInputType && fieldDefinition.isNullable && fieldDefinition.initialValue == null) {
            addIsDefinedFieldWithGetters(fieldDefinition, javaType)
        }
    }

    private fun addIsDefinedFieldWithGetters(fieldDefinition: Field, javaType: TypeSpec.Builder) {
        val fieldName = generateBooleanFieldName(ReservedKeywordSanitizer.sanitize(fieldDefinition.name))
        val field = FieldSpec
            .builder(com.squareup.javapoet.TypeName.BOOLEAN, fieldName)
            .addModifiers(Modifier.PRIVATE)
            .initializer("false")
            .build()

        val getter = MethodSpec
            .methodBuilder(fieldName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(JsonIgnore::class.java)
            .returns(com.squareup.javapoet.TypeName.BOOLEAN)
            .addStatement(
                "return \$N",
                fieldName
            ).build()
        javaType.addField(field)
        javaType.addMethod(getter)
    }

    private fun generateBooleanFieldName(name: String): String {
        return "is${name.capitalized()}Set"
    }

    private fun addFieldWithGetterAndSetter(returnType: JavaTypeName, fieldDefinition: Field, javaType: TypeSpec.Builder, isInputType: Boolean) {
        val fieldBuilder = if (fieldDefinition.initialValue != null) {
            FieldSpec
                .builder(fieldDefinition.type, ReservedKeywordSanitizer.sanitize(fieldDefinition.name))
                .addModifiers(Modifier.PRIVATE)
                .initializer(fieldDefinition.initialValue)
        } else {
            FieldSpec.builder(returnType, ReservedKeywordSanitizer.sanitize(fieldDefinition.name)).addModifiers(Modifier.PRIVATE)
        }

        if (fieldDefinition.description != null) {
            fieldBuilder.addJavadoc("\$L", fieldDefinition.description.content)
        }

        val getterPrefix = if (returnType == com.squareup.javapoet.TypeName.BOOLEAN && config.generateIsGetterForPrimitiveBooleanFields) "is" else "get"
        val getterName = typeUtils.transformIfDefaultClassMethodExists("${getterPrefix}${fieldDefinition.name[0].uppercase()}${fieldDefinition.name.substring(1)}", TypeUtils.getClass)

        val getterMethodBuilder = MethodSpec.methodBuilder(getterName).addModifiers(Modifier.PUBLIC).returns(returnType).addStatement("return \$N", ReservedKeywordSanitizer.sanitize(fieldDefinition.name))
        if (fieldDefinition.overrideGetter) {
            getterMethodBuilder.addAnnotation(Override::class.java)
        }

        if (fieldDefinition.description != null) {
            getterMethodBuilder.addJavadoc("\$L", fieldDefinition.description.content)
        }

        val setterName = typeUtils.transformIfDefaultClassMethodExists("set${fieldDefinition.name[0].uppercase()}${fieldDefinition.name.substring(1)}", TypeUtils.setClass)
        val parameterBuilder = ParameterSpec.builder(returnType, ReservedKeywordSanitizer.sanitize(fieldDefinition.name))

        val setterMethodBuilder = MethodSpec.methodBuilder(setterName)
            .addModifiers(Modifier.PUBLIC)
            .addStatement(
                "this.\$N = \$N",
                ReservedKeywordSanitizer.sanitize(fieldDefinition.name),
                ReservedKeywordSanitizer.sanitize(fieldDefinition.name)
            )
        if (config.generateIsSetFields && isInputType && fieldDefinition.isNullable && fieldDefinition.initialValue == null) {
            setterMethodBuilder
                .addStatement(
                    "this.\$N = true",
                    generateBooleanFieldName(ReservedKeywordSanitizer.sanitize(fieldDefinition.name))
                )
        }

        if (fieldDefinition.directives.isNotEmpty()) {
            val (annotations, comments) = applyDirectivesJava(fieldDefinition.directives, config)
            if (!comments.isNullOrBlank()) {
                fieldBuilder.addJavadoc("\$L", comments)
            }
            for ((key, value) in annotations) {
                when (SiteTarget.valueOf(key)) {
                    SiteTarget.FIELD -> fieldBuilder.addAnnotations(value)
                    SiteTarget.GET -> getterMethodBuilder.addAnnotations(value)
                    SiteTarget.SET -> setterMethodBuilder.addAnnotations(value)
                    SiteTarget.SETPARAM -> parameterBuilder.addAnnotations(value)
                    SiteTarget.PARAM -> continue
                    else -> fieldBuilder.addAnnotations(value)
                }
            }
        }
        setterMethodBuilder.addParameter(parameterBuilder.build())

        javaType.addField(fieldBuilder.build())
        javaType.addMethod(getterMethodBuilder.build())
        javaType.addMethod(setterMethodBuilder.build())
    }

    private fun addAbstractGetter(returnType: JavaTypeName, fieldDefinition: Field, javaType: TypeSpec.Builder) {
        val getterPrefix = if (returnType == JavaTypeName.BOOLEAN && config.generateIsGetterForPrimitiveBooleanFields) "is" else "get"
        val getterName = "${getterPrefix}${fieldDefinition.name[0].uppercase()}${fieldDefinition.name.substring(1)}"
        javaType.addMethod(
            MethodSpec.methodBuilder(getterName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(returnType).build()
        )
    }

    private fun addBuilder(fields: List<Field>, javaType: TypeSpec.Builder, isInputType: Boolean = false) {
        val builtType = javaType.build()
        val name = builtType.name
        val className = ClassName.get(packageName, name)

        val buildMethod = MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC)
            .returns(className)
            .addStatement("\$T result = new \$T()", className, className)
        for (fieldSpec in builtType.fieldSpecs) {
            buildMethod.addStatement("result.\$N = this.\$N", fieldSpec.name, fieldSpec.name)
        }
        buildMethod.addStatement("return result")

        val builderClassName = className.nestedClass("Builder")
        val newBuilderMethod =
            MethodSpec
                .methodBuilder("newBuilder")
                .returns(builderClassName)
                .addStatement("return new \$T()", builderClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .build()

        javaType.addMethod(newBuilderMethod)

        val builderType =
            TypeSpec
                .classBuilder(builderClassName)
                .addOptionalGeneratedAnnotation(config)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(buildMethod.build())

        builtType.fieldSpecs.forEach {
            builderType.addField(it)
            val method = MethodSpec.methodBuilder(it.name)
                .addJavadoc(it.javadoc)
                .returns(builderClassName)
                .addStatement("this.\$N = \$N", it.name, it.name)

            val fieldName = it.name
            val field = fields.find { iter -> ReservedKeywordSanitizer.sanitize(iter.name) == fieldName }
            if (config.generateIsSetFields && isInputType && field?.isNullable == true && field?.initialValue == null) {
                method
                    .addStatement(
                        "this.\$N = true",
                        generateBooleanFieldName(it.name)
                    )
            }

            method
                .addStatement("return this")
                .addParameter(ParameterSpec.builder(it.type, it.name).build())
                .addModifiers(Modifier.PUBLIC).build()

            builderType.addMethod(method.build())
        }

        javaType.addType(builderType.build())
    }
}
