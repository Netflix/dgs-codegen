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

import graphql.language.NullValue
import graphql.language.ObjectField
import graphql.language.ObjectValue
import graphql.language.Value
import graphql.schema.Coercing
import kotlin.reflect.full.allSuperclasses

class NullableInputValueSerializer(
    scalars: Map<Class<*>, Coercing<*, *>> = emptyMap(),
) : InputValueSerializer(scalars) {
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
                .map { (name, value) -> ObjectField(name, toValue(value)) }
                .toList()
        return ObjectValue
            .newObjectValue()
            .objectFields(objectFields)
            .build()
    }
}
