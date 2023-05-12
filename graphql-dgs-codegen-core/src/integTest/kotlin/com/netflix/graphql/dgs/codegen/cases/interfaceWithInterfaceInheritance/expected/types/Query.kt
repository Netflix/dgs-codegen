package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.collections.List
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
    fruits: () -> List<Fruit?>? = fruitsDefault
) {
    private val _fruits: () -> List<Fruit?>? = fruits

    @get:JvmName("getFruits")
    public val fruits: List<Fruit?>?
        get() = _fruits.invoke()

    public companion object {
        private val fruitsDefault: () -> List<Fruit?>? = { throw IllegalStateException("Field `fruits` was not requested") }
    }

    @JsonPOJOBuilder
    @JsonIgnoreProperties("__typename")
    public class Builder {
        private var fruits: () -> List<Fruit?>? = fruitsDefault

        @JsonProperty("fruits")
        public fun withFruits(fruits: List<Fruit?>?): Builder = this.apply {
            this.fruits = { fruits }
        }

        public fun build() = Query(
            fruits = fruits
        )
    }
}
