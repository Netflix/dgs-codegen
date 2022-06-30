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

import graphql.language.ArrayValue
import graphql.language.AstPrinter
import graphql.language.BooleanValue
import graphql.language.EnumValue
import graphql.language.FloatValue
import graphql.language.IntValue
import graphql.language.NullValue
import graphql.language.ObjectField
import graphql.language.ObjectValue
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.Currency
import java.util.Date
import java.util.TimeZone
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
        return AstPrinter.printAst(toValue(input))
    }

    fun toValue(input: Any?): Value<*> {
        if (input == null) {
            return NullValue.newNullValue().build()
        }

        if (input is Value<*>) {
            return input
        }

        if (input::class.java in scalars) {
            return scalars.getValue(input::class.java).valueToLiteral(input)
        }

        if (input::class in toStringClasses) {
            return StringValue.of(input.toString())
        }

        if (input is String) {
            return StringValue.of(input)
        }

        if (input is Float) {
            return FloatValue.of(input.toDouble())
        }

        if (input is Double) {
            return FloatValue.of(input)
        }

        if (input is BigDecimal) {
            return FloatValue.newFloatValue(input).build()
        }

        if (input is BigInteger) {
            return IntValue.newIntValue(input).build()
        }

        if (input is Int) {
            return IntValue.of(input)
        }

        if (input is Number) {
            return IntValue.newIntValue(BigInteger.valueOf(input.toLong())).build()
        }

        if (input is Boolean) {
            return BooleanValue.of(input)
        }

        if (input is Enum<*>) {
            return EnumValue.newEnumValue(input.name).build()
        }

        if (input is Collection<*>) {
            return ArrayValue.newArrayValue()
                .values(input.map { toValue(it) })
                .build()
        }

        if (input is Map<*, *>) {
            return ObjectValue.newObjectValue()
                .objectFields(input.map { (key, value) -> ObjectField(key.toString(), toValue(value)) })
                .build()
        }

        val classes = sequenceOf(input::class) + input::class.allSuperclasses.asSequence() - Any::class
        val propertyValues = mutableMapOf<String, Any?>()

        for (klass in classes) {
            for (property in klass.memberProperties) {
                if (property.name in propertyValues || property.isAbstract || property.hasAnnotation<Transient>()) {
                    continue
                }

                property.isAccessible = true
                propertyValues[property.name] = property.call(input)
            }
        }

        val objectFields = propertyValues.asSequence()
            .filter { (_, value) -> value != null }
            .map { (name, value) -> ObjectField(name, toValue(value)) }
            .toList()
        return ObjectValue.newObjectValue()
            .objectFields(objectFields)
            .build()
    }
}
