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
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import graphql.introspection.Introspection.TypeNameMetaFieldDef
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
            "value", "\$L",
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
