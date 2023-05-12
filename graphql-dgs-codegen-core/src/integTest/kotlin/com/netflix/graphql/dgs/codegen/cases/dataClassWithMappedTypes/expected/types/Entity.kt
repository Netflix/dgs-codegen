package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import java.time.OffsetDateTime
import kotlin.Long
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Entity.Builder::class)
public class Entity(
    long: () -> Long? = longDefault,
    dateTime: () -> OffsetDateTime? = dateTimeDefault
) {
    private val _long: () -> Long? = long

    private val _dateTime: () -> OffsetDateTime? = dateTime

    @get:JvmName("getLong")
    public val long: Long?
        get() = _long.invoke()

    @get:JvmName("getDateTime")
    public val dateTime: OffsetDateTime?
        get() = _dateTime.invoke()

    public companion object {
        private val longDefault: () -> Long? = { throw IllegalStateException("Field `long` was not requested") }

        private val dateTimeDefault: () -> OffsetDateTime? = { throw IllegalStateException("Field `dateTime` was not requested") }
    }

    @JsonPOJOBuilder
    @JsonIgnoreProperties("__typename")
    public class Builder {
        private var long: () -> Long? = longDefault

        private var dateTime: () -> OffsetDateTime? = dateTimeDefault

        @JsonProperty("long")
        public fun withLong(long: Long?): Builder = this.apply {
            this.long = { long }
        }

        @JsonProperty("dateTime")
        public fun withDateTime(dateTime: OffsetDateTime?): Builder = this.apply {
            this.dateTime = { dateTime }
        }

        public fun build() = Entity(
            long = long,
            dateTime = dateTime
        )
    }
}
