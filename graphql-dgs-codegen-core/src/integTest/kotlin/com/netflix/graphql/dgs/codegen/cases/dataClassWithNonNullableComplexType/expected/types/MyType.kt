package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableComplexType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = MyType.Builder::class)
public class MyType(
    other: () -> OtherType = otherDefault
) {
    private val _other: () -> OtherType = other

    @get:JvmName("getOther")
    public val other: OtherType
        get() = _other.invoke()

    public companion object {
        private val otherDefault: () -> OtherType = { throw IllegalStateException("Field `other` was not requested") }
    }

    @JsonPOJOBuilder
    @JsonIgnoreProperties("__typename")
    public class Builder {
        private var other: () -> OtherType = otherDefault

        @JsonProperty("other")
        public fun withOther(other: OtherType): Builder = this.apply {
            this.other = { other }
        }

        public fun build() = MyType(
            other = other
        )
    }
}
