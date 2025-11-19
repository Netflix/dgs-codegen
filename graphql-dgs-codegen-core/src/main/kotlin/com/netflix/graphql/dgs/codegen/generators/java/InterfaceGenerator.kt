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
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import graphql.language.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.lang.model.element.Modifier

class InterfaceGenerator(
    private val config: CodeGenConfig,
    private val document: Document,
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(InterfaceGenerator::class.java)
    }

    private val javaReservedKeywordSanitizer = JavaReservedKeywordSanitizer()
    private val packageName = config.packageNameTypes
    private val typeUtils = TypeUtils(packageName, config, document)
    private val useInterfaceType = config.generateInterfaces

    fun generate(
        definition: InterfaceTypeDefinition,
        extensions: List<InterfaceTypeExtensionDefinition>,
    ): CodeGenResult {
        if (definition.shouldSkip(config)) {
            return CodeGenResult.EMPTY
        }

        logger.info("Generating type {}", definition.name)
        val javaType =
            TypeSpec
                .interfaceBuilder(definition.name)
                .addOptionalGeneratedAnnotation(config)
                .addModifiers(Modifier.PUBLIC)

        if (config.generateJSpecifyAnnotations) {
            javaType.addAnnotation(jspecifyNullMarkedAnnotation())
        }

        if (definition.description != null) {
            javaType.addJavadoc("\$L", definition.description.content)
        }

        definition.implements
            .filterIsInstance<TypeName>()
            .forEach {
                javaType.addSuperinterface(typeUtils.findJavaInterfaceName(it.name, packageName))
            }

        val mergedFieldDefinitions = definition.fieldDefinitions + extensions.flatMap { it.fieldDefinitions }

        mergedFieldDefinitions.filterSkipped().forEach {
            // Generate getters/setters for fields that are not interfaces, and only getters for fields that are interfaces
            // unless generateInterfaceMethodsForInterfaceFields && generateInterfaceSetters.
            // Skip generating interface methods with list types where the inner type is an interface as Java does not
            // support overriding them with more specific types (i.e. List<Dog> does not override List<Pet>).
            //
            // interface Pet {
            // 	 parent: Pet
            // }
            // type Dog implements Pet {
            // 	 parent: Dog
            // }
            // type Bird implements Pet {
            // 	 parent: Bird
            // }
            // For the schema above, we currently generate Dog::setParent(Dog dog), but the interface
            // would have Pet::setParent(Pet pet) leading to missing overrides in the generated
            // implementation classes. This is not an issue if the overridden field has the same base type,
            // however.
            // Ref: https://github.com/graphql/graphql-js/issues/776
            if (!isListOfInterface(it.type) || config.generateInterfaceMethodsForInterfaceFields) {
                addInterfaceMethod(it, javaType)
            }
        }

        val implementations =
            document
                .getDefinitionsOfType(ObjectTypeDefinition::class.java)
                .asSequence()
                .filter { node -> node.implements.any { it.isEqualTo(TypeName(definition.name)) } }
                .map { node ->
                    typeUtils.findJavaInterfaceName(node.name, packageName)
                }.filterIsInstance<ClassName>()
                .toList()

        // Add JsonSubType annotations only if there are no generated concrete types that implement the interface
        if (implementations.isNotEmpty() && config.generateDataTypes) {
            javaType.addAnnotation(jsonTypeInfoAnnotation())
            javaType.addAnnotation(jsonSubTypeAnnotation(implementations))
        }

        val javaFile = JavaFile.builder(packageName, javaType.build()).build()

        return CodeGenResult(javaInterfaces = listOf(javaFile))
    }

    private fun isFieldAnInterface(fieldDefinition: FieldDefinition): Boolean =
        document
            .getDefinitionsOfType(InterfaceTypeDefinition::class.java)
            .any { node -> node.name == typeUtils.findInnerType(fieldDefinition.type).name }

    // Returns true if the field is a list type (possibly nested or non-null) with an innermost type that is an interface
    private fun isListOfInterface(fieldType: Type<*>): Boolean =
        when (fieldType) {
            is ListType -> {
                val innerType = typeUtils.findInnerType(fieldType)
                document
                    .getDefinitionsOfType(InterfaceTypeDefinition::class.java)
                    .any { node -> node.name == innerType.name }
            }
            is NonNullType -> isListOfInterface(fieldType.type)
            else -> false
        }

    private fun addInterfaceMethod(
        fieldDefinition: FieldDefinition,
        javaType: TypeSpec.Builder,
    ) {
        val returnType = typeUtils.findReturnType(fieldDefinition.type, useInterfaceType, true)

        val fieldName = fieldDefinition.name
        val getterPrefix =
            if (returnType == com.squareup.javapoet.TypeName.BOOLEAN &&
                config.generateIsGetterForPrimitiveBooleanFields
            ) {
                "is"
            } else {
                "get"
            }
        val getterBuilder =
            MethodSpec
                .methodBuilder(
                    typeUtils.transformIfDefaultClassMethodExists(
                        "${getterPrefix}${fieldName.capitalized()}",
                        TypeUtils.Companion.GET_CLASS,
                    ),
                ).addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(returnType)
        if (fieldDefinition.description != null) {
            getterBuilder.addJavadoc("\$L", fieldDefinition.description.content)
        }

        // Add @Nullable annotation for nullable return types when JSpecify is enabled
        if (config.generateJSpecifyAnnotations && fieldDefinition.type !is NonNullType) {
            getterBuilder.addAnnotation(jspecifyNullableAnnotation())
        }

        javaType.addMethod(getterBuilder.build())

        if (config.generateInterfaceSetters && (config.generateInterfaceMethodsForInterfaceFields || !isFieldAnInterface(fieldDefinition))
        ) {
            val parameterBuilder =
                ParameterSpec
                    .builder(returnType, javaReservedKeywordSanitizer.sanitize(fieldName))

            // Add @Nullable annotation for nullable parameters when JSpecify is enabled
            if (config.generateJSpecifyAnnotations && fieldDefinition.type !is NonNullType) {
                parameterBuilder.addAnnotation(jspecifyNullableAnnotation())
            }

            val setterBuilder =
                MethodSpec
                    .methodBuilder(
                        typeUtils.transformIfDefaultClassMethodExists("set${fieldName.capitalized()}", TypeUtils.Companion.SET_CLASS),
                    ).addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .addParameter(parameterBuilder.build())

            if (fieldDefinition.description != null) {
                setterBuilder.addJavadoc("\$L", fieldDefinition.description.content)
            }
            javaType.addMethod(setterBuilder.build())
        }
    }
}
