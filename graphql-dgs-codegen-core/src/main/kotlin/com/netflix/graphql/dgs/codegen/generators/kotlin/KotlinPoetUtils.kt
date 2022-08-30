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
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.netflix.graphql.dgs.codegen.generators.shared.PackageParserUtil
import com.netflix.graphql.dgs.codegen.generators.shared.ParserConstants
import com.netflix.graphql.dgs.codegen.generators.shared.generatedAnnotationClassName
import com.netflix.graphql.dgs.codegen.generators.shared.generatedDate
import com.squareup.kotlinpoet.*
import graphql.introspection.Introspection
import graphql.language.ArrayValue
import graphql.language.BooleanValue
import graphql.language.Description
import graphql.language.EnumValue
import graphql.language.FloatValue
import graphql.language.IntValue
import graphql.language.NullValue
import graphql.language.ObjectField
import graphql.language.ObjectValue
import graphql.language.StringValue
import graphql.language.Value
import java.lang.IllegalArgumentException

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

/**
 * Generate a [JvmName] annotation for a kotlin property.
 *
 * Example generated annotation:
 * ```
 * @JvmName("getIsRequired")
 * ```
 */
fun jvmNameAnnotation(name: String): AnnotationSpec {
    return AnnotationSpec.builder(JvmName::class)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
        .addMember("%S", "get${name.capitalized()}")
        .build()
}

/**
 * Generate a [Suppress] annotation for a kotlin property.
 * See: https://youtrack.jetbrains.com/issue/KT-31420
 *
 * Example generated annotation:
 * ```
 * @Suppress("INAPPLICABLE_JVM_NAME")
 * ```
 */
fun suppressInapplicableJvmNameAnnotation(): AnnotationSpec {
    return AnnotationSpec.builder(Suppress::class)
        .addMember("%S", "INAPPLICABLE_JVM_NAME")
        .build()
}

private fun generatedAnnotation(packageName: String): List<AnnotationSpec> {
    val graphqlGenerated = AnnotationSpec
        .builder(ClassName(packageName, "Generated"))
        .build()

    return if (generatedAnnotationClassName == null) {
        listOf(graphqlGenerated)
    } else {
        val generatedAnnotation = ClassName.bestGuess(generatedAnnotationClassName)

        val javaxGenerated = AnnotationSpec.builder(generatedAnnotation)
            .addMember("value = [%S]", CodeGen::class.qualifiedName!!)
            .addMember("date = %S", generatedDate)
            .build()

        listOf(javaxGenerated, graphqlGenerated)
    }
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

fun deprecatedAnnotation(reason: String): AnnotationSpec {
    // TODO support for level
    val replace = reason.substringAfter(ParserConstants.REPLACE_WITH_STR, "")
    val builder: AnnotationSpec.Builder = AnnotationSpec.builder(Deprecated::class)
        .addMember("${ParserConstants.MESSAGE}${ParserConstants.ASSIGNMENT_OPERATOR}%S", reason.substringBefore(ParserConstants.REPLACE_WITH_STR))
    if (replace.isNotEmpty()) {
        builder.addMember(CodeBlock.of("${ParserConstants.REPLACE_WITH}${ParserConstants.ASSIGNMENT_OPERATOR}%M(%S)", MemberName("kotlin", ParserConstants.REPLACE_WITH_CLASS), reason.substringAfter(ParserConstants.REPLACE_WITH_STR)))
    }
    return builder.build()
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

/**
 * Creates custom annotation from arguments
 * name -> Name of the class to be annotated. It will contain className with oor without the package name (Mandatory)
 * type -> The type of operation intended with this annotation. This value is also used to look up if there is any default packages associated with this annotation in the config
 * inputs -> These are the input parameters needed for the annotation. If empty no inputs will be present for the annotation
 */
fun customAnnotation(annotationArgumentMap: MutableMap<String, Value<Value<*>>>, config: CodeGenConfig): AnnotationSpec {
    if (annotationArgumentMap.isEmpty() || !annotationArgumentMap.containsKey(ParserConstants.NAME) || annotationArgumentMap[ParserConstants.NAME] is NullValue || (annotationArgumentMap[ParserConstants.NAME] as StringValue).value.isEmpty()) {
        throw IllegalArgumentException("Invalid annotate directive")
    }
    val (packageName, simpleName) = PackageParserUtil.getAnnotationPackage(
        config,
        (annotationArgumentMap[ParserConstants.NAME] as StringValue).value,
        if (annotationArgumentMap.containsKey(ParserConstants.TYPE) && annotationArgumentMap[ParserConstants.TYPE] !is NullValue) (annotationArgumentMap[ParserConstants.TYPE] as StringValue).value else null
    )
    val className = ClassName(packageName = packageName, simpleNames = listOf(simpleName))
    val annotation: AnnotationSpec.Builder = AnnotationSpec.builder(className)
    if (annotationArgumentMap.containsKey(ParserConstants.INPUTS)) {
        val codeBlocks: List<CodeBlock> = parseInputs(config, annotationArgumentMap[ParserConstants.INPUTS] as ObjectValue)
        codeBlocks.forEach { codeBlock ->
            annotation.addMember(codeBlock)
        }
    }
    return annotation.build()
}

/**
 * Generates the code block containing the parameters of an annotation in the format key = value
 */
private fun generateCode(config: CodeGenConfig, value: Value<Value<*>>, prefix: String = "", type: String = ""): CodeBlock =
    when (value) {
        is BooleanValue -> CodeBlock.of("$prefix%L", (value as BooleanValue).isValue)
        is IntValue -> CodeBlock.of("$prefix%L", (value as IntValue).value)
        is StringValue -> CodeBlock.of("$prefix%S", (value as StringValue).value)
        is FloatValue -> CodeBlock.of("$prefix%L", (value as FloatValue).value)
        // In an enum value the prefix/type (key in the parameters map for the enum) is used to get the package name from the config
        // Limitation: Since it uses the enum key to lookup the package from the configs. 2 enums using different packages cannot have the same keys.
        is EnumValue -> CodeBlock.of(
            "$prefix%M",
            MemberName(
                if (prefix.isNotEmpty()) config.includeImports.getOrDefault(prefix.substringBefore(ParserConstants.ASSIGNMENT_OPERATOR), "")
                else config.includeImports.getOrDefault(type.substringBefore(ParserConstants.ASSIGNMENT_OPERATOR), ""),
                (value as EnumValue).name
            )
        )
        is ArrayValue ->
            if ((value as ArrayValue).values.isEmpty()) CodeBlock.of("[]")
            else CodeBlock.of("$prefix[%L]", (value as ArrayValue).values.joinToString { v -> generateCode(config = config, value = v, type = if (v is EnumValue) prefix else "").toString() })
        else -> CodeBlock.of("$prefix%L", value)
    }

/**
 * Parses the inputs argument in the directive to get the input parameters of the annotation
 */
private fun parseInputs(config: CodeGenConfig, inputs: ObjectValue): List<CodeBlock> {
    val objectFields: List<ObjectField> = inputs.objectFields
    return objectFields.fold(mutableListOf()) { codeBlocks, objectField ->
        codeBlocks.add(generateCode(config, objectField.value, objectField.name + ParserConstants.ASSIGNMENT_OPERATOR))
        codeBlocks
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
            generatedAnnotation(config.packageName).forEach { addAnnotation(it) }
        }
    }
