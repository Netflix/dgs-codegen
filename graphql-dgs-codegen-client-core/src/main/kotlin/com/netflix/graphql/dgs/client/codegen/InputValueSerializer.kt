/*
 * Copyright 2021 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.graphql.dgs.client.codegen

import graphql.schema.Coercing
import java.time.*
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Marks this property invisible for input value serialization.
 */
@Target(AnnotationTarget.PROPERTY)
internal annotation class Transient

class InputValueSerializer(private val scalars: Map<Class<*>, Coercing<*, *>> = emptyMap()) {
    companion object {
        private val toStringClasses = setOf(
            String::class,
            LocalDateTime::class,
            LocalDate::class,
            LocalTime::class,
            TimeZone::class,
            Date::class,
            OffsetDateTime::class,
            Currency::class,
            Instant::class
        )
    }

    fun serialize(input: Any?): String {
        if (input == null) {
            return "null"
        }

        return if (input::class.java in scalars) {
            """"${scalars.getValue(input::class.java).serialize(input)}""""
        } else if (input::class.javaPrimitiveType != null) {
            input.toString()
        } else if (input::class.java.isEnum) {
            (input as Enum<*>).name
        } else if (input::class in toStringClasses) {
            // Call toString for known types, in case no scalar is found. This is for backward compatibility.
            """"${input.toString().replace("\\", "\\\\").replace("\"", "\\\"")}""""
        } else if (input is Collection<*>) {
            """[${input.filterNotNull().joinToString(", ") { listItem -> serialize(listItem) }}]"""
        } else if (input is Map<*, *>) {
            input.entries.joinToString(", ", "{ ", " }") { (key, value) ->
                if (value != null) {
                    """$key: ${serialize(value)}"""
                } else {
                    """$key: null"""
                }
            }
        } else {
            val classes = sequenceOf(input::class) + input::class.allSuperclasses.asSequence() - Any::class
            val properties = mutableMapOf<String, KProperty1<*, *>>()

            for (klass in classes) {
                for (property in klass.memberProperties) {
                    if (property.name in properties || property.isAbstract || property.hasAnnotation<Transient>()) {
                        continue
                    }

                    property.isAccessible = true
                    properties[property.name] = property
                }
            }

            properties.values.asSequence()
                .mapNotNull { property ->
                    val value = property.call(input)
                    value?.let { """${property.name}:${serialize(value)}""" }
                }.joinToString(", ", "{", "}")
        }
    }
}
