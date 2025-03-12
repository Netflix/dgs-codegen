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
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.templatedClassName
import com.netflix.graphql.dgs.codegen.shouldSkip
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import graphql.language.Document
import graphql.language.FieldDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.InterfaceTypeExtensionDefinition
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.lang.model.element.Modifier

class InterfaceGenerator(private val config: CodeGenConfig, private val document: Document) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(InterfaceGenerator::class.java)
    }

    private val packageName = config.packageNameTypes
    private val typeUtils = TypeUtils(packageName, config, document)
    private val useInterfaceType = config.generateInterfaces

    fun generate(
        definition: InterfaceTypeDefinition,
        extensions: List<InterfaceTypeExtensionDefinition>
    ): CodeGenResult {
        if (definition.shouldSkip(config)) {
            return CodeGenResult.EMPTY
        }

        logger.info("Generating type {}", definition.name)
        val javaType = TypeSpec.interfaceBuilder(definition.templatedClassName(config.nameTemplate))
            .addOptionalGeneratedAnnotation(config)
            .addModifiers(Modifier.PUBLIC)

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
            // Only generate getters/setters for fields that are not interfaces.
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
            if (!isFieldAnInterface(it) || config.generateInterfaceMethodsForInterfaceFields) {
                addInterfaceMethod(it, javaType)
            }
        }

        val implementations = document.getDefinitionsOfType(ObjectTypeDefinition::class.java).asSequence()
            .filter { node -> node.implements.any { it.isEqualTo(TypeName(definition.name)) } }
            .associate {
                it.name to typeUtils
                    .findJavaInterfaceName(it.templatedClassName(config.nameTemplate), packageName) as? ClassName
            }
            .mapNotNull { (name, className) -> className?.let { name to it } }
            .toMap()

        // Add JsonSubType annotations only if there are no generated concrete types that implement the interface
        if (implementations.isNotEmpty() && config.generateDataTypes) {
            javaType.addAnnotation(jsonTypeInfoAnnotation())
            javaType.addAnnotation(jsonSubTypeAnnotation(implementations))
        }

        val javaFile = JavaFile.builder(packageName, javaType.build()).build()

        return CodeGenResult(javaInterfaces = listOf(javaFile))
    }

    private fun isFieldAnInterface(fieldDefinition: FieldDefinition): Boolean {
        return document.getDefinitionsOfType(InterfaceTypeDefinition::class.java)
            .any { node -> node.name == typeUtils.findInnerType(fieldDefinition.type).name }
    }

    private fun addInterfaceMethod(fieldDefinition: FieldDefinition, javaType: TypeSpec.Builder) {
        val returnType = typeUtils.findReturnType(fieldDefinition.type, useInterfaceType, true)

        val fieldName = fieldDefinition.name
        val getterPrefix = if (returnType == com.squareup.javapoet.TypeName.BOOLEAN && config.generateIsGetterForPrimitiveBooleanFields) "is" else "get"
        val getterBuilder = MethodSpec.methodBuilder(typeUtils.transformIfDefaultClassMethodExists("${getterPrefix}${fieldName.capitalized()}", TypeUtils.Companion.getClass))
            .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
            .returns(returnType)
        if (fieldDefinition.description != null) {
            getterBuilder.addJavadoc("\$L", fieldDefinition.description.content)
        }
        javaType.addMethod(getterBuilder.build())

        if (config.generateInterfaceSetters) {
            val setterBuilder = MethodSpec.methodBuilder(typeUtils.transformIfDefaultClassMethodExists("set${fieldName.capitalized()}", TypeUtils.Companion.setClass))
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .addParameter(returnType, ReservedKeywordSanitizer.sanitize(fieldName))

            if (fieldDefinition.description != null) {
                setterBuilder.addJavadoc("\$L", fieldDefinition.description.content)
            }
            javaType.addMethod(setterBuilder.build())
        }
    }
}
