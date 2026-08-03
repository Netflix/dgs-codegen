package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.Generated
import java.lang.IllegalStateException
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
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

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__fruits === fruitsDefault) fruitsDefault else fruits,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Query &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__fruits === fruitsDefault) null else "fruits=" + fruits,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Query(", postfix =
      ")")

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
