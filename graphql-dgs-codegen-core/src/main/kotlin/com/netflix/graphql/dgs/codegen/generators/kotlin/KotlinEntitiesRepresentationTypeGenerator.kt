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

package com.netflix.graphql.dgs.codegen.generators.kotlin

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.fieldDefinitions
import com.netflix.graphql.dgs.codegen.generators.EntitiesRepresentationTypeGeneratorUtils
import com.netflix.graphql.dgs.codegen.generators.EntitiesRepresentationTypeGeneratorUtils.findType
import com.netflix.graphql.dgs.codegen.generators.EntitiesRepresentationTypeGeneratorUtils.toRepresentationName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import graphql.language.Document
import graphql.language.EnumTypeDefinition
import graphql.language.FieldDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.ObjectTypeDefinition
import org.slf4j.LoggerFactory

class KotlinEntitiesRepresentationTypeGenerator(config: CodeGenConfig, document: Document) :
    AbstractKotlinDataTypeGenerator(packageName = config.packageNameClient, config = config, document = document) {

    fun generate(
        definition: ObjectTypeDefinition,
        generatedRepresentations: MutableMap<String, Any>
    ): CodeGenResult {
        return EntitiesRepresentationTypeGeneratorUtils.generate(
            config,
            definition,
            generatedRepresentations,
            this::generateRepresentations
        )
    }

    private fun generateRepresentations(
        definitionName: String,
        representationName: String,
        fields: List<FieldDefinition>,
        generatedRepresentations: MutableMap<String, Any>,
        keyFields: Map<String, Any>
    ): CodeGenResult {
        if (representationName in generatedRepresentations) {
            return CodeGenResult()
        }
        var fieldsCodeGenAccumulator = CodeGenResult()
        // generate representations of entity types that have @key, including the __typename field, and the  key fields
        val typeName = Field("__typename", STRING, false, CodeBlock.of("%S", definitionName))
        val fieldDefinitions =
            fields
                .filter { keyFields.containsKey(it.name) }
                .map {
                    val type = findType(it.type, document)
                    if (type != null &&
                        (
                            type is ObjectTypeDefinition ||
                                type is InterfaceTypeDefinition ||
                                type is EnumTypeDefinition
                            )
                    ) {
                        val fieldType = typeUtils.findReturnType(it.type)
                        val fieldTypeRepresentationName = toRepresentationName(type)
                        val fieldRepresentationType =
                            fieldType
                                .toString()
                                .replace(type.name, fieldTypeRepresentationName).removeSuffix("?")

                        if (generatedRepresentations.containsKey(fieldTypeRepresentationName)) {
                            logger.trace("Representation fo $fieldTypeRepresentationName was already generated.")
                        } else {
                            logger.debug("Generating entity representation {} ...", fieldTypeRepresentationName)
                            val fieldTypeRepresentation = generateRepresentations(
                                type.name,
                                fieldTypeRepresentationName,
                                type.fieldDefinitions(),
                                generatedRepresentations,
                                keyFields[it.name] as Map<String, Any>
                            )
                            fieldsCodeGenAccumulator = fieldsCodeGenAccumulator.merge(fieldTypeRepresentation)
                            generatedRepresentations[fieldTypeRepresentationName] = fieldRepresentationType
                        }
                        if (fieldType is ParameterizedTypeName && fieldType.rawType.simpleName == "List") {
                            Field(
                                it.name,
                                LIST.parameterizedBy(ClassName(getPackageName(), fieldTypeRepresentationName)),
                                typeUtils.isNullable(it.type)
                            )
                        } else {
                            Field(
                                it.name,
                                ClassName(getPackageName(), fieldTypeRepresentationName),
                                typeUtils.isNullable(it.type)
                            )
                        }
                    } else {
                        Field(it.name, typeUtils.findReturnType(it.type), typeUtils.isNullable(it.type))
                    }
                }
        // Generate base type representation...
        val parentRepresentationCodeGen = super.generate(
            name = representationName,
            interfaces = emptyList(),
            fields = fieldDefinitions.plus(typeName),
            description = null,
            document = document
        )
        generatedRepresentations[representationName] = typeUtils.qualifyName(representationName)
        // Merge all results.
        return parentRepresentationCodeGen.merge(fieldsCodeGenAccumulator)
    }

    override fun getPackageName(): String {
        return config.packageNameClient
    }

    companion object {
        private val logger: org.slf4j.Logger =
            LoggerFactory.getLogger(KotlinEntitiesRepresentationTypeGenerator::class.java)
    }
}
