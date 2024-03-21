package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullablePrimitiveInList.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.collections.List
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = MyType.Builder::class)
public class MyType(
  count: () -> List<Int>? = countDefault,
  truth: () -> List<Boolean>? = truthDefault,
  floaty: () -> List<Double>? = floatyDefault,
) {
  private val _count: () -> List<Int>? = count

  private val _truth: () -> List<Boolean>? = truth

  private val _floaty: () -> List<Double>? = floaty

  @get:JvmName("getCount")
  public val count: List<Int>?
    get() = _count.invoke()

  @get:JvmName("getTruth")
  public val truth: List<Boolean>?
    get() = _truth.invoke()

  @get:JvmName("getFloaty")
  public val floaty: List<Double>?
    get() = _floaty.invoke()

  public companion object {
    private val countDefault: () -> List<Int>? = 
        { throw IllegalStateException("Field `count` was not requested") }


    private val truthDefault: () -> List<Boolean>? = 
        { throw IllegalStateException("Field `truth` was not requested") }


    private val floatyDefault: () -> List<Double>? = 
        { throw IllegalStateException("Field `floaty` was not requested") }

  }

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
