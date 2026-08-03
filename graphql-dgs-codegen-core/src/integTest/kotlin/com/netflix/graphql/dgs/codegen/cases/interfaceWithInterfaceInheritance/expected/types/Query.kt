package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  fruits: () -> List<Fruit?>? = fruitsDefault,
) {
  private val __fruits: () -> List<Fruit?>? = fruits

  @get:JvmName("getFruits")
  public val fruits: List<Fruit?>?
    get() = __fruits.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Query) return false
    return (__fruits === fruitsDefault) == (other.__fruits === fruitsDefault) && (__fruits ===
        fruitsDefault || Objects.equals(fruits, other.fruits))
  }

  override fun hashCode(): Int = Objects.hash(if (__fruits === fruitsDefault) fruitsDefault else
      fruits)

  @Generated
  public companion object {
    private val fruitsDefault: () -> List<Fruit?>? = 
        { throw IllegalStateException("Field `fruits` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var fruits: () -> List<Fruit?>? = fruitsDefault

    @JsonProperty("fruits")
    public fun withFruits(fruits: List<Fruit?>?): Builder = this.apply {
      this.fruits = { fruits }
    }

    public fun build(): Query = Query(
      fruits = fruits,
    )
  }
}
