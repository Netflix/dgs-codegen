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
import com.netflix.graphql.dgs.codegen.generators.shared.JAKARTA_GENERATED_ANNOTATION
import com.netflix.graphql.dgs.codegen.generators.shared.PackageParserUtil
import com.netflix.graphql.dgs.codegen.generators.shared.ParserConstants
import com.netflix.graphql.dgs.codegen.generators.shared.generatedDate
import com.palantir.javapoet.AnnotationSpec
import com.palantir.javapoet.ClassName
import com.palantir.javapoet.CodeBlock
import com.palantir.javapoet.TypeName
import com.palantir.javapoet.TypeSpec
import com.palantir.javapoet.WildcardTypeName
import graphql.introspection.Introspection.TypeNameMetaFieldDef
import graphql.language.ArrayValue
import graphql.language.BooleanValue
import graphql.language.EnumValue
import graphql.language.FloatValue
import graphql.language.IntValue
import graphql.language.NullValue
import graphql.language.ObjectField
import graphql.language.ObjectValue
import graphql.language.StringValue
import graphql.language.Value

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
fun jsonTypeInfoAnnotation(): AnnotationSpec =
    AnnotationSpec
        .builder(JsonTypeInfo::class.java)
        .addMember("use", "\$T.\$L", JsonTypeInfo.Id::class.java, JsonTypeInfo.Id.NAME.name)
        .addMember("include", "\$T.\$L", JsonTypeInfo.As::class.java, JsonTypeInfo.As.PROPERTY.name)
        .addMember("property", "\$S", TypeNameMetaFieldDef.name)
        .build()

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
fun disableJsonTypeInfoAnnotation(): AnnotationSpec =
    AnnotationSpec
        .builder(JsonTypeInfo::class.java)
        .addMember("use", "\$T.\$L", JsonTypeInfo.Id::class.java, JsonTypeInfo.Id.NONE.name)
        .build()

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
            AnnotationSpec
                .builder(JsonSubTypes.Type::class.java)
                .addMember("value", "\$T.class", type)
                .addMember("name", "\$S", type.simpleName())
                .build(),
        )
    }

    return annotationSpec.build()
}

/**
 * Generate a JSpecify `@Nullable` annotation.
 */
fun jspecifyNullableAnnotation(): AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jspecify.annotations", "Nullable")).build()

/**
 * Generate a JSpecify `@NullMarked` annotation.
 */
fun jspecifyNullMarkedAnnotation(): AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jspecify.annotations", "NullMarked")).build()

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

private fun generatedAnnotation(
    packageName: String,
    generateDate: Boolean,
    generatedAnnotationType: String?,
): AnnotationSpec {
    if (generatedAnnotationType == null) {
        return AnnotationSpec
            .builder(ClassName.get(packageName, "Generated"))
            .build()
    }

    val builder = AnnotationSpec.builder(ClassName.bestGuess(generatedAnnotationType))

    if (generatedAnnotationType == JAKARTA_GENERATED_ANNOTATION) {
        builder.addMember("value", "${'$'}S", CodeGen::class.qualifiedName!!)
        if (generateDate) {
            builder.addMember("date", "${'$'}S", generatedDate)
        }
    }

    return builder.build()
}

/**
 * `@SuppressWarnings("this-escape")` for generated constructors that populate state owned by the client base classes,
 * e.g. `getInput().put(...)` in a `GraphQLQuery` constructor or `getFields().put(...)` in a projection initializer.
 *
 * javac 21+ reports these calls under `-Xlint:this-escape` (and so fails builds using `-Xlint:all -Werror`) because
 * the generated classes are public and non-final. The escape is benign: the accessors are final and only touch maps
 * that the superclass constructor already created. Older javac versions ignore the unknown key.
 */
internal val suppressThisEscapeWarning: AnnotationSpec =
    AnnotationSpec
        .builder(SuppressWarnings::class.java)
        .addMember("value", "${'$'}S", "this-escape")
        .build()

fun TypeSpec.Builder.addOptionalGeneratedAnnotation(config: CodeGenConfig): TypeSpec.Builder =
    apply {
        if (config.addGeneratedAnnotation) {
            addAnnotation(
                generatedAnnotation(
                    config.packageName,
                    !config.disableDatesInGeneratedAnnotation,
                    config.generatedAnnotationType,
                ),
            )
        }
    }

/**
 * Creates custom annotation from arguments
 * name -> Name of the class to be annotated. It will contain className with oor without the package name (Mandatory)
 * type -> The type of operation intended with this annotation. This value is also used to look up if there is any default packages associated with this annotation in the config
 * inputs -> These are the input parameters needed for the annotation. If empty no inputs will be present for the annotation
 */
fun customAnnotation(
    annotationArgumentMap: MutableMap<String, Value<Value<*>>>,
    config: CodeGenConfig,
): AnnotationSpec {
    if (annotationArgumentMap.isEmpty() ||
        !annotationArgumentMap.containsKey(ParserConstants.NAME) ||
        annotationArgumentMap[ParserConstants.NAME] is NullValue ||
        (annotationArgumentMap[ParserConstants.NAME] as StringValue).value.isEmpty()
    ) {
        throw IllegalArgumentException("Invalid annotate directive")
    }
    val (packageName, simpleName) =
        PackageParserUtil.getAnnotationPackage(
            config,
            (annotationArgumentMap[ParserConstants.NAME] as StringValue).value,
            if (annotationArgumentMap.containsKey(ParserConstants.TYPE) &&
                annotationArgumentMap[ParserConstants.TYPE] !is NullValue
            ) {
                (annotationArgumentMap[ParserConstants.TYPE] as StringValue).value
            } else {
                null
            },
        )
    val className = ClassName.get(packageName, simpleName)
    val annotation: AnnotationSpec.Builder = AnnotationSpec.builder(className)
    if (annotationArgumentMap.containsKey(ParserConstants.INPUTS)) {
        val objectFields: List<ObjectField> = (annotationArgumentMap[ParserConstants.INPUTS] as ObjectValue).objectFields
        for (objectField in objectFields) {
            val codeBlock: CodeBlock =
                generateCode(
                    config,
                    objectField.value,
                    (annotationArgumentMap[ParserConstants.NAME] as StringValue).value,
                    objectField.name,
                )
            annotation.addMember(objectField.name, codeBlock)
        }
    }
    return annotation.build()
}

/**
 * Generates the code block containing the parameters of an annotation in the format value
 */
private fun generateCode(
    config: CodeGenConfig,
    value: Value<Value<*>>,
    annotationName: String,
    prefix: String = "",
): CodeBlock =
    when (value) {
        is BooleanValue -> {
            CodeBlock.of("\$L", value.isValue)
        }

        is IntValue -> {
            CodeBlock.of("\$L", value.value)
        }

        is StringValue -> {
            // If string value ends with .class and classImports mapping is provided, treat as Java Class
            val string = value.value
            if (string.endsWith(ParserConstants.CLASS_STRING)) {
                val className = string.dropLast(ParserConstants.CLASS_LENGTH)
                // Use annotationName and className in the PackagerParserUtil to get Class Package name.
                val classPackage = PackageParserUtil.getClassPackage(config, annotationName, className)
                if (classPackage.isNotEmpty()) {
                    CodeBlock.of("\$T.class", ClassName.get(classPackage, className))
                } else {
                    CodeBlock.of("\$S", string)
                }
            } else {
                CodeBlock.of("\$S", string)
            }
        }

        is FloatValue -> {
            CodeBlock.of("\$L", value.value)
        }

        // In an enum value the prefix (key in the parameters map for the enum) is used to get the package name from the config
        // Limitation: Since it uses the enum key to lookup the package from the configs. 2 enums using different packages cannot have the same keys.
        is EnumValue -> {
            CodeBlock.of(
                "\$T",
                ClassName.get(PackageParserUtil.getEnumPackage(config, annotationName, prefix), value.name),
            )
        }

        is ArrayValue -> {
            if (value.values.isEmpty()) {
                CodeBlock.of("{}")
            } else {
                CodeBlock.of(
                    "{\$L}",
                    value.values.joinToString { v ->
                        generateCode(
                            config = config,
                            value = v,
                            annotationName = annotationName,
                            prefix = if (v is EnumValue) prefix else "",
                        ).toString()
                    },
                )
            }
        }

        else -> {
            CodeBlock.of("\$L", value)
        }
    }

private fun typeClassBestGuess(name: String): TypeName =
    when (name) {
        "String" -> ClassName.get("java.lang", "String")
        "Integer" -> ClassName.INT.box()
        "Long" -> ClassName.LONG.box()
        "Float" -> ClassName.FLOAT.box()
        "Double" -> ClassName.DOUBLE.box()
        "Character" -> ClassName.CHAR.box()
        "Short" -> ClassName.SHORT.box()
        "Byte" -> ClassName.BYTE.box()
        "Number" -> ClassName.get("java.lang", "Number")
        "Boolean" -> ClassName.BOOLEAN.box()
        "Object" -> ClassName.OBJECT
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

/** Preserve Javadoc markup while escaping prose that would break doclint or end the Java comment. */
internal fun String.escapeJavaDocComparisons(): String =
    buildString {
        var offset = 0
        while (offset < this@escapeJavaDocComparisons.length) {
            val end = this@escapeJavaDocComparisons.javaDocMarkupEnd(offset)
            if (end > offset) {
                append(this@escapeJavaDocComparisons.substring(offset, end).escapeInlineJavaDocSourceEscapes())
                offset = end
            } else {
                val character = this@escapeJavaDocComparisons[offset++]
                when {
                    character == '<' -> append("&lt;")
                    character == '&' && bareJavaDocAmpersand.matchesAt(this@escapeJavaDocComparisons, offset - 1) -> append("&amp;")
                    else -> append(character)
                }
            }
        }
    }.replace("*/", "*&#47;").replace(javaDocUnicodeEscape) { "${it.groupValues[1]}&#92;" }

private val bareJavaDocAmpersand = Regex("&(?!([a-zA-Z][a-zA-Z0-9]*|#[0-9]+|#[xX][0-9a-fA-F]+);)")

// Java decodes Unicode escapes before recognizing comments; only odd runs of backslashes before u are eligible.
private val javaDocUnicodeEscape = Regex("""(?<!\\)((?:\\\\)*)\\(?=u)""")

private fun String.escapeInlineJavaDocSourceEscapes(): String {
    val code = startsWith("{@code")
    if (!code && !startsWith("{@literal")) return this
    if (!contains("*/") && !javaDocUnicodeEscape.containsMatchIn(this)) return this

    // Entities inside inline literals render verbatim, so use equivalent HTML when Java source needs escaping.
    val content =
        substring(if (code) 6 else 9, lastIndex)
            .removePrefix(" ")
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("{", "&#123;")
            .replace("@", "&#64;")
    return if (code) "<code>$content</code>" else content
}

private fun String.javaDocMarkupEnd(start: Int): Int {
    if ((startsWith("{@code", start) && getOrNull(start + 6)?.isWhitespace() == true) ||
        (startsWith("{@literal", start) && getOrNull(start + 9)?.isWhitespace() == true)
    ) {
        var depth = 1
        for (offset in start + 1 until length) {
            when (this[offset]) {
                '{' -> depth++
                '}' -> if (--depth == 0) return offset + 1
            }
        }
        return -1
    }
    if (startsWith("<!--", start)) {
        val end = indexOf("-->", start + 4)
        return if (end == -1) -1 else end + 3
    }
    return if (this[start] == '<') htmlTagEnd(start) else -1
}

private fun String.htmlTagEnd(start: Int): Int {
    var offset = start + 1
    val closing = getOrNull(offset) == '/'
    if (closing) offset++
    val nameStart = offset
    while (getOrNull(offset)?.let { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' } == true) offset++
    if (substring(nameStart, offset).lowercase() !in javaDocHtmlTags) return -1

    if (closing) {
        while (getOrNull(offset)?.isWhitespace() == true) offset++
        return if (getOrNull(offset) == '>') offset + 1 else -1
    }

    while (offset < length) {
        val beforeWhitespace = offset
        while (getOrNull(offset)?.isWhitespace() == true) offset++
        if (getOrNull(offset) == '>') return offset + 1
        if (startsWith("/>", offset)) return offset + 2
        if (offset == beforeWhitespace) return -1

        val attributeStart = offset
        while (getOrNull(offset)?.let { it.isLetterOrDigit() || it in "_:-." } == true) offset++
        if (offset == attributeStart) return -1
        val attributeEnd = offset
        while (getOrNull(offset)?.isWhitespace() == true) offset++
        if (getOrNull(offset) != '=') {
            offset = attributeEnd
            continue
        }

        offset++
        while (getOrNull(offset)?.isWhitespace() == true) offset++
        val quote = getOrNull(offset) ?: return -1
        if (quote == '\'' || quote == '"') {
            val end = indexOf(quote, offset + 1)
            if (end == -1) return -1
            offset = end + 1
        } else {
            val valueStart = offset
            while (offset < length && !this[offset].isWhitespace() && this[offset] != '>') {
                if (this[offset] in "<\"'`=") return -1
                offset++
            }
            if (offset == valueStart) return -1
        }
    }
    return -1
}

// Java 17 doclint names keep generic type arguments such as <String> from being mistaken for HTML.
private val javaDocHtmlTags =
    (
        "a abbr acronym address article aside b bdi big blockquote body br caption center cite code col colgroup " +
            "dd del dfn div dl dt em font footer figure figcaption frame frameset h1 h2 h3 h4 h5 h6 head header hr html " +
            "i iframe img ins kbd li link main mark menu meta nav noframes noscript ol p pre q s samp script section " +
            "small span strong sub sup table tbody td template tfoot th thead time title tr tt u ul var wbr"
    ).split(' ').toSet()
