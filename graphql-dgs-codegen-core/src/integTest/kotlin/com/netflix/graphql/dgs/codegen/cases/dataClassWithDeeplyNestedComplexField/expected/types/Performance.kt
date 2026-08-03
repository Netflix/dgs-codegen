package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.Generated
import java.lang.IllegalStateException
import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.collections.List
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

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__zeroToSixty === zeroToSixtyDefault) zeroToSixtyDefault else zeroToSixty,
      if (__quarterMile === quarterMileDefault) quarterMileDefault else quarterMile,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Performance &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__zeroToSixty === zeroToSixtyDefault) null else "zeroToSixty=" + zeroToSixty,
      if (__quarterMile === quarterMileDefault) null else "quarterMile=" + quarterMile,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Performance(",
      postfix = ")")

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
