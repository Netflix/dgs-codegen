package com.netflix.graphql.dgs.codegen.generators.kotlin

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import graphql.introspection.Introspection

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
            .addMember("property = %S",  Introspection.TypeNameMetaFieldDef.name)
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
            prefix = "value = [\n⇥", postfix = "⇤\n]") { "%L" }

    return AnnotationSpec.builder(JsonSubTypes::class)
            .addMember(formatString, *subTypeAnnotations.toTypedArray())
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
    return AnnotationSpec.builder(JsonProperty::class.java)
            .addMember("%S", name)
            .build()
}