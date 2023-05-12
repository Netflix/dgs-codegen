package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Engine.Builder::class)
public class Engine(
    type: () -> String? = typeDefault,
    bhp: () -> Int? = bhpDefault,
    size: () -> Double? = sizeDefault,
    performance: () -> Performance? = performanceDefault
) {
    private val _type: () -> String? = type

    private val _bhp: () -> Int? = bhp

    private val _size: () -> Double? = size

    private val _performance: () -> Performance? = performance

    @get:JvmName("getType")
    public val type: String?
        get() = _type.invoke()

    @get:JvmName("getBhp")
    public val bhp: Int?
        get() = _bhp.invoke()

    @get:JvmName("getSize")
    public val size: Double?
        get() = _size.invoke()

    @get:JvmName("getPerformance")
    public val performance: Performance?
        get() = _performance.invoke()

    public companion object {
        private val typeDefault: () -> String? = { throw IllegalStateException("Field `type` was not requested") }

        private val bhpDefault: () -> Int? = { throw IllegalStateException("Field `bhp` was not requested") }

        private val sizeDefault: () -> Double? = { throw IllegalStateException("Field `size` was not requested") }

        private val performanceDefault: () -> Performance? = { throw IllegalStateException("Field `performance` was not requested") }
    }

    @JsonPOJOBuilder
    @JsonIgnoreProperties("__typename")
    public class Builder {
        private var type: () -> String? = typeDefault

        private var bhp: () -> Int? = bhpDefault

        private var size: () -> Double? = sizeDefault

        private var performance: () -> Performance? = performanceDefault

        @JsonProperty("type")
        public fun withType(type: String?): Builder = this.apply {
            this.type = { type }
        }

        @JsonProperty("bhp")
        public fun withBhp(bhp: Int?): Builder = this.apply {
            this.bhp = { bhp }
        }

        @JsonProperty("size")
        public fun withSize(size: Double?): Builder = this.apply {
            this.size = { size }
        }

        @JsonProperty("performance")
        public fun withPerformance(performance: Performance?): Builder = this.apply {
            this.performance = { performance }
        }

        public fun build() = Engine(
            type = type,
            bhp = bhp,
            size = size,
            performance = performance
        )
    }
}
