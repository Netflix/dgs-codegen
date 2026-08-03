package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullablePrimitiveInList.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullablePrimitiveInList.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = MyType.Builder::class)
public class MyType(
  count: () -> List<Int>? = countDefault,
  truth: () -> List<Boolean>? = truthDefault,
  floaty: () -> List<Double>? = floatyDefault,
) {
  private val __count: () -> List<Int>? = count

  private val __truth: () -> List<Boolean>? = truth

  private val __floaty: () -> List<Double>? = floaty

  @get:JvmName("getCount")
  public val count: List<Int>?
    get() = __count.invoke()

  @get:JvmName("getTruth")
  public val truth: List<Boolean>?
    get() = __truth.invoke()

  @get:JvmName("getFloaty")
  public val floaty: List<Double>?
    get() = __floaty.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MyType) return false
    return (__count === countDefault) == (other.__count === countDefault) && (__count ===
        countDefault || Objects.equals(count, other.count)) &&
    (__truth === truthDefault) == (other.__truth === truthDefault) && (__truth === truthDefault ||
        Objects.equals(truth, other.truth)) &&
    (__floaty === floatyDefault) == (other.__floaty === floatyDefault) && (__floaty ===
        floatyDefault || Objects.equals(floaty, other.floaty))
  }

  override fun hashCode(): Int = Objects.hash(if (__count === countDefault) countDefault else count,
  if (__truth === truthDefault) truthDefault else truth,
  if (__floaty === floatyDefault) floatyDefault else floaty)

  @Generated
  public companion object {
    private val countDefault: () -> List<Int>? = 
        { throw IllegalStateException("Field `count` was not requested") }

    private val truthDefault: () -> List<Boolean>? = 
        { throw IllegalStateException("Field `truth` was not requested") }

    private val floatyDefault: () -> List<Double>? = 
        { throw IllegalStateException("Field `floaty` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var count: () -> List<Int>? = countDefault

    private var truth: () -> List<Boolean>? = truthDefault

    private var floaty: () -> List<Double>? = floatyDefault

    @JsonProperty("count")
    public fun withCount(count: List<Int>?): Builder = this.apply {
      this.count = { count }
    }

    @JsonProperty("truth")
    public fun withTruth(truth: List<Boolean>?): Builder = this.apply {
      this.truth = { truth }
    }

    @JsonProperty("floaty")
    public fun withFloaty(floaty: List<Double>?): Builder = this.apply {
      this.floaty = { floaty }
    }

    public fun build(): MyType = MyType(
      count = count,
      truth = truth,
      floaty = floaty,
    )
  }
}
