package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.jvm.JvmName

@Generated
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Performance) return false
    return (__zeroToSixty === zeroToSixtyDefault) == (other.__zeroToSixty === zeroToSixtyDefault) &&
        (__zeroToSixty === zeroToSixtyDefault || Objects.equals(zeroToSixty, other.zeroToSixty)) &&
    (__quarterMile === quarterMileDefault) == (other.__quarterMile === quarterMileDefault) &&
        (__quarterMile === quarterMileDefault || Objects.equals(quarterMile, other.quarterMile))
  }

  override fun hashCode(): Int = Objects.hash(if (__zeroToSixty === zeroToSixtyDefault)
      zeroToSixtyDefault else zeroToSixty,
  if (__quarterMile === quarterMileDefault) quarterMileDefault else quarterMile)

  @Generated
  public companion object {
    private val zeroToSixtyDefault: () -> Double? = 
        { throw IllegalStateException("Field `zeroToSixty` was not requested") }

    private val quarterMileDefault: () -> Double? = 
        { throw IllegalStateException("Field `quarterMile` was not requested") }
  }

  @Generated
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
