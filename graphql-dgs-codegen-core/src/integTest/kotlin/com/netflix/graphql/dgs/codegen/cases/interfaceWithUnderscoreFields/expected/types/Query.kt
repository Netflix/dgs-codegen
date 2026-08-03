package com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.Generated
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
  `is`: () -> List<I?>? = isDefault,
) {
  private val __is: () -> List<I?>? = `is`

  @get:JvmName("getIs")
  public val `is`: List<I?>?
    get() = __is.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Query) return false
    return (__is === isDefault) == (other.__is === isDefault) && (__is === isDefault ||
        Objects.equals(`is`, other.`is`))
  }

  override fun hashCode(): Int = Objects.hash(if (__is === isDefault) isDefault else `is`)

  @Generated
  public companion object {
    private val isDefault: () -> List<I?>? = 
        { throw IllegalStateException("Field `is` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var `is`: () -> List<I?>? = isDefault

    @JsonProperty("is")
    public fun withIs(`is`: List<I?>?): Builder = this.apply {
      this.`is` = { `is` }
    }

    public fun build(): Query = Query(
      `is` = `is`,
    )
  }
}
