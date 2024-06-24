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

package com.netflix.graphql.dgs.codegen.generators.shared

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.generators.kotlin.KotlinTypeUtils
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.TypeName
import graphql.language.*

fun generateKotlinCode(
    value: Value<Value<*>>,
    type: TypeName,
    inputTypeDefinitions: Collection<InputObjectTypeDefinition>,
    config: CodeGenConfig,
    typeUtils: KotlinTypeUtils
): CodeBlock {
    return when (value) {
        is BooleanValue -> CodeBlock.of("%L", value.isValue)
        is IntValue -> CodeBlock.of("%L", value.value)
        is StringValue -> {
            val localeValueOverride = checkAndGetLocaleValue(value, type)
            if (localeValueOverride != null) CodeBlock.of("%L", localeValueOverride)
            else CodeBlock.of("%S", value.value)
        }
        is FloatValue -> CodeBlock.of("%L", value.value)
        is EnumValue -> CodeBlock.of("%M", MemberName(type.className, value.name))
        is ArrayValue ->
            if (value.values.isEmpty()) CodeBlock.of("emptyList()")
            else CodeBlock.of(
                "listOf(%L)",
                value.values.joinToString { v -> generateKotlinCode(v, type, inputTypeDefinitions, config, typeUtils).toString() }
            )

        is ObjectValue -> {
            val inputObjectDefinition = inputTypeDefinitions.first {
                val expectedCanonicalClassName = config.typeMapping[it.name] ?: "${config.packageNameTypes}.${it.name}"
                expectedCanonicalClassName == type.className.canonicalName
            }

            CodeBlock.of(
                type.className.canonicalName + "(%L)",
                value.objectFields.joinToString { objectProperty ->
                    val argumentType =
                        checkNotNull(inputObjectDefinition.inputValueDefinitions.find { it.name == objectProperty.name }) {
                            "Property \"${objectProperty.name}\" does not exist in input type \"${inputObjectDefinition.name}\""
                        }
                    "${objectProperty.name} = ${
                    generateKotlinCode(
                        objectProperty.value,
                        typeUtils.findReturnType(argumentType.type),
                        inputTypeDefinitions,
                        config,
                        typeUtils
                    )
                    }"
                }
            )
        }

        else -> CodeBlock.of("%L", value)
    }
}

private fun checkAndGetLocaleValue(value: StringValue, type: TypeName): String? {
    if (type.className.canonicalName == "java.util.Locale") return "Locale.forLanguageTag(\"${value.value}\")"
    return null
}

private val TypeName.className: ClassName
    get() = when (this) {
        is ClassName -> this
        is ParameterizedTypeName -> typeArguments[0].className
        else -> TODO()
    }
