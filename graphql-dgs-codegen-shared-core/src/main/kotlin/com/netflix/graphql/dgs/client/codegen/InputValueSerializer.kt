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

import graphql.GraphQLContext
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
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

open class InputValueSerializer(
    private val scalars: Map<Class<*>, Coercing<*, *>> = emptyMap(),
    private val graphQLContext: GraphQLContext = GraphQLContext.getDefault(),
) : InputValueSerializerInterface {
    companion object {
        private val toStringClasses =
            setOf(
                String::class,
                LocalDateTime::class,
                LocalDate::class,
                LocalTime::class,
                TimeZone::class,
                Date::class,
                OffsetDateTime::class,
                Currency::class,
                Instant::class,
            )
    }

    override fun serialize(input: Any?): String = AstPrinter.printAst(toValue(input))

    override fun toValue(input: Any?): Value<*> {
        if (input == null) {
            return NullValue.newNullValue().build()
        }

        val optionalValue = getOptionalValue(input)

        if (optionalValue.isPresent) {
            return optionalValue.get()
        }

        val classes = (sequenceOf(input::class) + input::class.allSuperclasses.asSequence()) - Any::class
        val propertyValues = getPropertyValues(classes, input)

        val objectFields =
            propertyValues
                .asSequence()
                .filter { (_, value) -> value != null }
                .map { (name, value) -> ObjectField(InputReservedKeywordSanitizer().desanitize(name), toValue(value)) }
                .toList()
        return ObjectValue
            .newObjectValue()
            .objectFields(objectFields)
            .build()
    }

    protected fun getOptionalValue(input: Any): Optional<Value<*>> {
        if (input is Value<*>) {
            return Optional.of(input)
        }

        for (scalar in scalars.keys) {
            if (input::class.java == scalar || scalar.isAssignableFrom(input::class.java)) {
                return Optional.of(scalars[scalar]!!.valueToLiteral(input, graphQLContext, Locale.getDefault()))
            }
        }

        if (input::class in toStringClasses) {
            return Optional.of(StringValue.of(input.toString()))
        }

        if (input is String) {
            return Optional.of(StringValue.of(input))
        }

        if (input is Float) {
            return Optional.of(FloatValue.of(input.toDouble()))
        }

        if (input is Double) {
            return Optional.of(FloatValue.of(input))
        }

        if (input is BigDecimal) {
            return Optional.of(FloatValue.newFloatValue(input).build())
        }

        if (input is BigInteger) {
            return Optional.of(IntValue.newIntValue(input).build())
        }

        if (input is Int) {
            return Optional.of(IntValue.of(input))
        }

        if (input is Number) {
            return Optional.of(IntValue.newIntValue(BigInteger.valueOf(input.toLong())).build())
        }

        if (input is Boolean) {
            return Optional.of(BooleanValue.of(input))
        }

        if (input is Enum<*>) {
            return Optional.of(EnumValue.newEnumValue(input.name).build())
        }

        if (input is Collection<*>) {
            return Optional.of(
                ArrayValue
                    .newArrayValue()
                    .values(input.map { toValue(it) })
                    .build(),
            )
        }

        if (input is Map<*, *>) {
            return Optional.of(
                ObjectValue
                    .newObjectValue()
                    .objectFields(input.map { (key, value) -> ObjectField(key.toString(), toValue(value)) })
                    .build(),
            )
        }

        if (input is InputValue) {
            return Optional.of(
                ObjectValue
                    .newObjectValue()
                    .objectFields(input.inputValues().map { (name, value) -> ObjectField(name, toValue(value)) })
                    .build(),
            )
        }

        return Optional.empty()
    }

    protected fun getPropertyValues(
        classes: Sequence<KClass<out Any>>,
        input: Any?,
    ): MutableMap<String, Any?> {
        val propertyValues = mutableMapOf<String, Any?>()

        for (klass in classes) {
            for (property in klass.memberProperties) {
                if (property.name in propertyValues || property.isAbstract || property.hasAnnotation<Transient>()) {
                    continue
                }
                property.isAccessible = true
                if (property.returnType.classifier == Optional::class) {
                    val value = property.call(input)
                    if (value != null && value is Optional<*>) {
                        propertyValues[property.name] = value.orElse(null)
                    }
                    // if value is null, don't include the field
                } else {
                    propertyValues[property.name] = property.call(input)
                }
            }
        }
        return propertyValues
    }
}
