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

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.netflix.graphql.dgs.codegen.CodeGen
import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.generators.shared.PackageParserUtil
import com.netflix.graphql.dgs.codegen.generators.shared.ParserConstants
import com.netflix.graphql.dgs.codegen.generators.shared.generatedAnnotationClassName
import com.netflix.graphql.dgs.codegen.generators.shared.generatedDate
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
import graphql.introspection.Introspection.TypeNameMetaFieldDef
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

/**
 * Adds @Deprecated annotation
 */
fun deprecatedAnnotation(): AnnotationSpec {
    return AnnotationSpec.builder(java.lang.Deprecated::class.java).build()
}

fun jsonTypeInfoAnnotation(): AnnotationSpec {
    return AnnotationSpec.builder(JsonTypeInfo::class.java)
        .addMember("use", "\$T.\$L", JsonTypeInfo.Id::class.java, JsonTypeInfo.Id.NAME.name)
        .addMember("include", "\$T.\$L", JsonTypeInfo.As::class.java, JsonTypeInfo.As.PROPERTY.name)
        .addMember("property", "\$S", TypeNameMetaFieldDef.name)
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
    return AnnotationSpec.builder(JsonTypeInfo::class.java)
        .addMember("use", "\$T.\$L", JsonTypeInfo.Id::class.java, JsonTypeInfo.Id.NONE.name)
        .build()
}

/**
 * Generate a [JsonSubTypes] annotation for the supplied class names.
 *
 * Example generated annotation:
 *
 * ```
 *  @JsonSubTypes({
 *    JsonSubTypes.Type(value = Movie.class, name = "Movie"),
 *    JsonSubTypes.Type(value = Actor.class, name = "Actor")
 *  })
 *  ```
 */
fun jsonSubTypeAnnotation(subTypes: Collection<ClassName>): AnnotationSpec {
    val annotationSpec = AnnotationSpec.builder(JsonSubTypes::class.java)

    for (type in subTypes) {
        annotationSpec.addMember(
            "value",
            "\$L",
            AnnotationSpec.builder(JsonSubTypes.Type::class.java)
                .addMember("value", "\$T.class", type)
                .addMember("name", "\$S", type.simpleName())
                .build()
        )
    }

    return annotationSpec.build()
}

/**
 * Javapoet treats $ as a reference
 * https://github.com/square/javapoet/issues/670
 */
fun Description.sanitizeJavaDoc(): String {
    return this.content.lines().joinToString("\n").replace("$", "$$")
}

fun String.toTypeName(isGenericParam: Boolean = false): TypeName {
    val normalizedClassName = this.trim()

    if (!isGenericParam) {
        return typeClassBestGuess(normalizedClassName)
    }

    val superKeyword = normalizedClassName.split(" super ")
    val extendsKeyword = normalizedClassName.split(" extends ")

    return when {
        normalizedClassName == "?" -> WildcardTypeName.get(Object::class.java)
        superKeyword.size == 2 -> WildcardTypeName.supertypeOf(superKeyword[1].toTypeName())
        extendsKeyword.size == 2 -> WildcardTypeName.subtypeOf(extendsKeyword[1].toTypeName())
        else -> typeClassBestGuess(normalizedClassName)
    }
}

private fun generatedAnnotation(packageName: String): List<AnnotationSpec> {
    val graphqlGenerated = AnnotationSpec
        .builder(ClassName.get(packageName, "Generated"))
        .build()

    return if (generatedAnnotationClassName == null) {
        listOf(graphqlGenerated)
    } else {
        val generatedAnnotation = ClassName.bestGuess(generatedAnnotationClassName)

        val javaxGenerated = AnnotationSpec.builder(generatedAnnotation)
            .addMember("value", "${'$'}S", CodeGen::class.qualifiedName!!)
            .addMember("date", "${'$'}S", generatedDate)
            .build()

        listOf(javaxGenerated, graphqlGenerated)
    }
}

fun TypeSpec.Builder.addOptionalGeneratedAnnotation(config: CodeGenConfig): TypeSpec.Builder =
    apply {
        if (config.addGeneratedAnnotation) {
            generatedAnnotation(config.packageName).forEach { addAnnotation(it) }
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
    val className = ClassName.get(packageName, simpleName)
    val annotation: AnnotationSpec.Builder = AnnotationSpec.builder(className)
    if (annotationArgumentMap.containsKey(ParserConstants.INPUTS)) {
        val objectFields: List<ObjectField> = (annotationArgumentMap[ParserConstants.INPUTS] as ObjectValue).objectFields
        for (objectField in objectFields) {
            val codeBlock: CodeBlock = generateCode(
                config,
                objectField.value,
                (annotationArgumentMap[ParserConstants.NAME] as StringValue).value,
                objectField.name
            )
            annotation.addMember(objectField.name, codeBlock)
        }
    }
    return annotation.build()
}

/**
 * Generates the code block containing the parameters of an annotation in the format value
 */
private fun generateCode(config: CodeGenConfig, value: Value<Value<*>>, annotationName: String, prefix: String = ""): CodeBlock =
    when (value) {
        is BooleanValue -> CodeBlock.of("\$L", (value as BooleanValue).isValue)
        is IntValue -> CodeBlock.of("\$L", (value as IntValue).value)
        is StringValue ->
            // If string ends with .class, treat as class object
            if ((value as StringValue).value.takeLast(6) == ".class") {
                val className = (value as StringValue).value.dropLast(6)
                // Use annotationName and className in the PackagerParserUtil to get Class Package name.
                CodeBlock.of(
                    "\$T.class",
                    ClassName.get(PackageParserUtil.getClassPackage(config, annotationName, className), className)
                )
            }
            else CodeBlock.of("\$S", (value as StringValue).value)
        is FloatValue -> CodeBlock.of("\$L", (value as FloatValue).value)
        // In an enum value the prefix (key in the parameters map for the enum) is used to get the package name from the config
        // Limitation: Since it uses the enum key to lookup the package from the configs. 2 enums using different packages cannot have the same keys.
        is EnumValue -> CodeBlock.of(
            "\$T",
            ClassName.get(PackageParserUtil.getEnumPackage(config, annotationName, prefix), (value as EnumValue).name)
        )
        is ArrayValue ->
            if ((value as ArrayValue).values.isEmpty()) CodeBlock.of("[]")
            else CodeBlock.of("[\$L]", (value as ArrayValue).values.joinToString { v -> generateCode(config = config, value = v, annotationName = annotationName, prefix = if (v is EnumValue) prefix else "").toString() })
        else -> CodeBlock.of("\$L", value)
    }

private fun typeClassBestGuess(name: String): TypeName {
    return when (name) {
        "String" -> ClassName.get("java.lang", "String")
        "Integer" -> ClassName.get("java.lang", "Integer")
        "Long" -> ClassName.get("java.lang", "Long")
        "Float" -> ClassName.get("java.lang", "Float")
        "Double" -> ClassName.get("java.lang", "Double")
        "Character" -> ClassName.get("java.lang", "Character")
        "Short" -> ClassName.get("java.lang", "Short")
        "Byte" -> ClassName.get("java.lang", "Byte")
        "Number" -> ClassName.get("java.lang", "Number")
        "Boolean" -> ClassName.get("java.lang", "Boolean")
        "Object" -> ClassName.get("java.lang", "Object")
        "BigDecimal" -> ClassName.get("java.math", "BigDecimal")
        "List" -> ClassName.get("java.util", "List")
        "ArrayList" -> ClassName.get("java.util", "ArrayList")
        "LinkedList" -> ClassName.get("java.util", "LinkedList")
        "Map" -> ClassName.get("java.util", "Map")
        "HashMap" -> ClassName.get("java.util", "HashMap")
        "Set" -> ClassName.get("java.util", "Set")
        "HashSet" -> ClassName.get("java.util", "HashSet")
        "Queue" -> ClassName.get("java.util", "Queue")
        "TreeMap" -> ClassName.get("java.util", "TreeMap")
        else -> ClassName.bestGuess(name)
    }
}
