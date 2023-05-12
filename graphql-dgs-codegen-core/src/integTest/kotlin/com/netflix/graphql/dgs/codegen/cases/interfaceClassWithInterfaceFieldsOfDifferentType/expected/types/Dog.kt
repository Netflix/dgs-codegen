package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Dog.Builder::class)
public class Dog(
    name: () -> String? = nameDefault,
    diet: () -> Vegetarian? = dietDefault
) : Pet {
    private val _name: () -> String? = name

    private val _diet: () -> Vegetarian? = diet

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getName")
    public override val name: String?
        get() = _name.invoke()

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getDiet")
    public override val diet: Vegetarian?
        get() = _diet.invoke()

    public companion object {
        private val nameDefault: () -> String? = { throw IllegalStateException("Field `name` was not requested") }

        private val dietDefault: () -> Vegetarian? = { throw IllegalStateException("Field `diet` was not requested") }
    }

    @JsonPOJOBuilder
    @JsonIgnoreProperties("__typename")
    public class Builder {
        private var name: () -> String? = nameDefault

        private var diet: () -> Vegetarian? = dietDefault

        @JsonProperty("name")
        public fun withName(name: String?): Builder = this.apply {
            this.name = { name }
        }

        @JsonProperty("diet")
        public fun withDiet(diet: Vegetarian?): Builder = this.apply {
            this.diet = { diet }
        }

        public fun build() = Dog(
            name = name,
            diet = diet
        )
    }
}
