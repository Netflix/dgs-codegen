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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.CodeGen
import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.generators.shared.generatedAnnotationClassName
import com.netflix.graphql.dgs.codegen.generators.shared.generatedDate
import com.squareup.kotlinpoet.*
import graphql.introspection.Introspection
import graphql.language.Description

/**
 * Generate a [JsonTypeInfo] annotation, which allows for Jackson
 * polymorphic type handling when deserializing from JSON.
 *
 * Example generated annotation:
 * ```
 * @JsonTypeInfo(
 *   use = JsonTypeInfo.Id.NAME,
 *   include = JsonTypeInfo.As.PROPERTY,
 *   property = "__typename")
 * ```
 */
fun jsonTypeInfoAnnotation(): AnnotationSpec {
    return AnnotationSpec.builder(JsonTypeInfo::class)
        .addMember("use = %T.%L", JsonTypeInfo.Id::class, JsonTypeInfo.Id.NAME.name)
        .addMember("include = %T.%L", JsonTypeInfo.As::class, JsonTypeInfo.As.PROPERTY.name)
        .addMember("property = %S", Introspection.TypeNameMetaFieldDef.name)
        .build()
}

/**
 * Generate a [JsonTypeInfo] annotation, to explicitly disable
 * polymorphic type handling. This is mostly useful as a workaround
 * for cases where a user attempts to deserialize to a concrete type
 * from JSON that does not include the type id.
 *
 * **See also:** [Jackson issue](https://github.com/FasterXML/jackson-databind/issues/2968)
 *
 * Example generated annotation:
 * ```
 * @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
 * ```
 */
fun disableJsonTypeInfoAnnotation(): AnnotationSpec {
    return AnnotationSpec.builder(JsonTypeInfo::class)
        .addMember("use = %T.%L", JsonTypeInfo.Id::class, JsonTypeInfo.Id.NONE.name)
        .build()
}

/**
 * Generate a [JsonSubTypes] annotation for the supplied class names.
 *
 * Example generated annotation:
 *
 * ```
 *  @JsonSubTypes(value = [
 *    JsonSubTypes.Type(value = Movie::class, name = "Movie"),
 *    JsonSubTypes.Type(value = Actor::class, name = "Actor")
 *  ])
 *  ```
 */
fun jsonSubTypesAnnotation(subTypes: Collection<ClassName>): AnnotationSpec {
    val subTypeAnnotations = subTypes.map { type ->
        AnnotationSpec.builder(JsonSubTypes.Type::class)
            .addMember("value = %T::class", type)
            .addMember("name = %S", type.simpleName)
            .build()
    }

    val formatString = subTypes.joinToString(
        separator = ",\n",
        prefix = "value = [\n⇥",
        postfix = "⇤\n]"
    ) { "%L" }

    return AnnotationSpec.builder(JsonSubTypes::class)
        .addMember(formatString, *subTypeAnnotations.toTypedArray())
        .build()
}

/**
 * Generate a [JsonDeserialize] annotation for the builder class.
 *
 * Example generated annotation:
 * ```
 * @JsonDeserialize(builder = Movie.Builder::class)
 * ```
 */
fun jsonDeserializeAnnotation(builderType: ClassName): AnnotationSpec {
    return AnnotationSpec.builder(JsonDeserialize::class)
        .addMember("builder = %T::class", builderType)
        .build()
}

/**
 * Generate a [JsonPOJOBuilder] annotation for the builder class.
 *
 * Example generated annotation:
 * ```
 * @JsonPOJOBuilder
 * ```
 */
fun jsonBuilderAnnotation(): AnnotationSpec {
    return AnnotationSpec.builder(JsonPOJOBuilder::class)
        .build()
}

@Suppress("DuplicatedCode") // not duplicated - this is KotlinPoet, the other is JavaPoet
private fun generatedAnnotation(): AnnotationSpec? {
    val generatedAnnotation = generatedAnnotationClassName
        ?.let { ClassName.bestGuess(it) }
        ?: return null

    return AnnotationSpec.builder(generatedAnnotation)
        .addMember("value = [%S]", CodeGen::class.qualifiedName!!)
        .addMember("date = %S", generatedDate)
        .build()
}

/**
 * Generate a [JsonProperty] annotation for the supplied
 * field name.
 *
 * Example generated annotation:
 * ```
 * @JsonProperty("fieldName")
 * ```
 */
fun jsonPropertyAnnotation(name: String): AnnotationSpec {
    return AnnotationSpec.builder(JsonProperty::class)
        .addMember("%S", name)
        .build()
}

/**
 * Generate a [JsonIgnoreProperties] annotation for the supplied
 * property name.
 *
 * Example generated annotation:
 * ```
 * @JsonIgnoreProperties("__typename")
 * ```
 */
fun jsonIgnorePropertiesAnnotation(name: String): AnnotationSpec {
    return AnnotationSpec.builder(JsonIgnoreProperties::class)
        .addMember("%S", name)
        .build()
}

fun Description.sanitizeKdoc(): String {
    return this.content.lineSequence().joinToString("\n")
}

fun String.toKtTypeName(isGenericParam: Boolean = false): TypeName {
    val normalizedClassName = this.trim()

    if (!isGenericParam) {
        ktTypeClassBestGuess(normalizedClassName)
    }

    return when {
        normalizedClassName == "*" -> STAR
        normalizedClassName.endsWith("?") -> ktTypeClassBestGuess(normalizedClassName.dropLast(1)).copy(nullable = true)
        else -> ktTypeClassBestGuess(normalizedClassName)
    }
}

private fun ktTypeClassBestGuess(name: String): ClassName {
    return when (name) {
        STRING.simpleName -> STRING
        INT.simpleName -> INT
        LONG.simpleName -> LONG
        CHAR.simpleName -> CHAR
        FLOAT.simpleName -> FLOAT
        DOUBLE.simpleName -> DOUBLE
        CHAR_SEQUENCE.simpleName -> CHAR_SEQUENCE
        BOOLEAN.simpleName -> BOOLEAN
        ANY.simpleName -> ANY
        SHORT.simpleName -> SHORT
        NUMBER.simpleName -> NUMBER
        LIST.simpleName -> LIST
        SET.simpleName -> SET
        MAP.simpleName -> MAP
        "BigDecimal" -> ClassName("java.math", "BigDecimal")
        MUTABLE_LIST.simpleName -> MUTABLE_LIST
        MUTABLE_SET.simpleName -> MUTABLE_SET
        MUTABLE_MAP.simpleName -> MUTABLE_MAP
        BYTE_ARRAY.simpleName -> BYTE_ARRAY
        CHAR_ARRAY.simpleName -> CHAR_ARRAY
        SHORT_ARRAY.simpleName -> SHORT_ARRAY
        INT_ARRAY.simpleName -> INT_ARRAY
        LONG_ARRAY.simpleName -> LONG_ARRAY
        FLOAT_ARRAY.simpleName -> FLOAT_ARRAY
        DOUBLE_ARRAY.simpleName -> DOUBLE_ARRAY
        else -> ClassName.bestGuess(name)
    }
}

fun FunSpec.Builder.addControlFlow(
    controlFlow: String,
    vararg args: Any,
    builder: FunSpec.Builder.() -> Unit
): FunSpec.Builder {
    this.beginControlFlow(controlFlow, *args)
    builder.invoke(this)
    this.endControlFlow()
    return this
}

fun TypeSpec.Builder.addEnumConstants(enumSpecs: Iterable<TypeSpec>): TypeSpec.Builder = apply {
    enumSpecs.map { addEnumConstant(it.name!!, it) }
}

fun TypeSpec.Builder.addOptionalGeneratedAnnotation(config: CodeGenConfig): TypeSpec.Builder =
    apply {
        if (config.addGeneratedAnnotation) {
            generatedAnnotation()?.also { addAnnotation(it) }
        }
    }
