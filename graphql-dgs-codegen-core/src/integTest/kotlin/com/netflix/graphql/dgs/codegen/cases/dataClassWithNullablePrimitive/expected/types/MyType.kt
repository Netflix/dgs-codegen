package com.netflix.graphql.dgs.codegen.cases.dataClassWithNullablePrimitive.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = MyType.Builder::class)
public class MyType(
  count: () -> Int? = countDefault,
  truth: () -> Boolean? = truthDefault,
  floaty: () -> Double? = floatyDefault,
) {
  private val __count: () -> Int? = count

  private val __truth: () -> Boolean? = truth

  private val __floaty: () -> Double? = floaty

  @get:JvmName("getCount")
  public val count: Int?
    get() = __count.invoke()

  @get:JvmName("getTruth")
  public val truth: Boolean?
    get() = __truth.invoke()

  @get:JvmName("getFloaty")
  public val floaty: Double?
    get() = __floaty.invoke()

  public companion object {
    private val countDefault: () -> Int? = 
        { throw IllegalStateException("Field `count` was not requested") }


    private val truthDefault: () -> Boolean? = 
        { throw IllegalStateException("Field `truth` was not requested") }


    private val floatyDefault: () -> Double? = 
        { throw IllegalStateException("Field `floaty` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var count: () -> Int? = countDefault

    private var truth: () -> Boolean? = truthDefault

    private var floaty: () -> Double? = floatyDefault

    @JsonProperty("count")
    public fun withCount(count: Int?): Builder = this.apply {
      this.count = { count }
    }

    @JsonProperty("truth")
    public fun withTruth(truth: Boolean?): Builder = this.apply {
      this.truth = { truth }
    }

    @JsonProperty("floaty")
    public fun withFloaty(floaty: Double?): Builder = this.apply {
      this.floaty = { floaty }
    }

    public fun build(): MyType = MyType(
      count = count,
      truth = truth,
      floaty = floaty,
    )
  }
}
