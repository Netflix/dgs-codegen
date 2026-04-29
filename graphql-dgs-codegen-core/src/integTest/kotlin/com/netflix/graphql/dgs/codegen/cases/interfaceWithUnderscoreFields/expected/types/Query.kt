package com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.Generated
import java.lang.IllegalStateException
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
