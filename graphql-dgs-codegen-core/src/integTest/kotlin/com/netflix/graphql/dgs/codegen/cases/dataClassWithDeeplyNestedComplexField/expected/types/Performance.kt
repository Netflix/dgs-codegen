package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.Double
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Performance.Builder::class)
public class Performance(
  zeroToSixty: () -> Double? = zeroToSixtyDefault,
  quarterMile: () -> Double? = quarterMileDefault,
) {
  private val __zeroToSixty: () -> Double? = zeroToSixty

  private val __quarterMile: () -> Double? = quarterMile

  @get:JvmName("getZeroToSixty")
  public val zeroToSixty: Double?
    get() = __zeroToSixty.invoke()

  @get:JvmName("getQuarterMile")
  public val quarterMile: Double?
    get() = __quarterMile.invoke()

  public companion object {
    private val zeroToSixtyDefault: () -> Double? = 
        { throw IllegalStateException("Field `zeroToSixty` was not requested") }


    private val quarterMileDefault: () -> Double? = 
        { throw IllegalStateException("Field `quarterMile` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var zeroToSixty: () -> Double? = zeroToSixtyDefault

    private var quarterMile: () -> Double? = quarterMileDefault

    @JsonProperty("zeroToSixty")
    public fun withZeroToSixty(zeroToSixty: Double?): Builder = this.apply {
      this.zeroToSixty = { zeroToSixty }
    }

    @JsonProperty("quarterMile")
    public fun withQuarterMile(quarterMile: Double?): Builder = this.apply {
      this.quarterMile = { quarterMile }
    }

    public fun build(): Performance = Performance(
      zeroToSixty = zeroToSixty,
      quarterMile = quarterMile,
    )
  }
}
