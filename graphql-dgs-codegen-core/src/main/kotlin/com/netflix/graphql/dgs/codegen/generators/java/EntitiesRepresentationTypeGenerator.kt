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
import com.netflix.graphql.dgs.codegen.fieldDefinitions
import com.netflix.graphql.dgs.codegen.generators.EntitiesRepresentationTypeGeneratorUtils
import com.netflix.graphql.dgs.codegen.generators.EntitiesRepresentationTypeGeneratorUtils.findType
import com.netflix.graphql.dgs.codegen.generators.EntitiesRepresentationTypeGeneratorUtils.toRepresentationName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import graphql.language.Document
import graphql.language.EnumTypeDefinition
import graphql.language.FieldDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.ObjectTypeDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("UNCHECKED_CAST")
class EntitiesRepresentationTypeGenerator(
    config: CodeGenConfig,
    document: Document,
) : BaseDataTypeGenerator(config.packageNameClient, config, document) {
    fun generate(
        definition: ObjectTypeDefinition,
        generatedRepresentations: MutableMap<String, Any>,
    ): CodeGenResult =
        EntitiesRepresentationTypeGeneratorUtils.generate(
            config,
            definition,
            generatedRepresentations,
            this::generateRepresentations,
        )

    private fun generateRepresentations(
        definitionName: String,
        representationName: String,
        fields: List<FieldDefinition>,
        generatedRepresentations: MutableMap<String, Any>,
        keyFields: Map<String, Any>,
    ): CodeGenResult {
        if (generatedRepresentations.containsKey(representationName)) {
            return CodeGenResult.EMPTY
        }
        var fieldsCodeGenAccumulator = CodeGenResult.EMPTY
        // generate representations of entity types that have @key, including the __typename field, and the  key fields
        val typeName = Field("__typename", ClassName.get(String::class.java), CodeBlock.of("\$S", definitionName))
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
                        val fieldTypeRepresentationName = toRepresentationName(type)
                        val fieldRepresentationType =
                            typeUtils
                                .findReturnType(it.type)
                                .toString()
                                .replace(type.name, fieldTypeRepresentationName)

                        if (generatedRepresentations.containsKey(fieldTypeRepresentationName)) {
                            logger.trace("Representation for {} was already generated.", fieldTypeRepresentationName)
                        } else {
                            logger.debug("Generating entity representation {} ...", fieldTypeRepresentationName)
                            val fieldTypeRepresentation =
                                generateRepresentations(
                                    type.name,
                                    fieldTypeRepresentationName,
                                    type.fieldDefinitions(),
                                    generatedRepresentations,
                                    keyFields[it.name] as Map<String, Any>,
                                )
                            fieldsCodeGenAccumulator = fieldsCodeGenAccumulator.merge(fieldTypeRepresentation)
                            generatedRepresentations[fieldTypeRepresentationName] = fieldRepresentationType
                        }
                        Field(it.name, ClassName.get("", fieldRepresentationType))
                    } else {
                        Field(it.name, typeUtils.findReturnType(it.type))
                    }
                }
        // Generate base type representation...
        val parentRepresentationCodeGen =
            super.generate(
                name = representationName,
                interfaces = emptyList(),
                fields = fieldDefinitions + typeName,
                description = null,
                directives = emptyList(),
            )
        generatedRepresentations[representationName] = typeUtils.qualifyName(representationName)
        // Merge all results.
        return parentRepresentationCodeGen.merge(fieldsCodeGenAccumulator)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(EntitiesRepresentationTypeGenerator::class.java)
    }
}
