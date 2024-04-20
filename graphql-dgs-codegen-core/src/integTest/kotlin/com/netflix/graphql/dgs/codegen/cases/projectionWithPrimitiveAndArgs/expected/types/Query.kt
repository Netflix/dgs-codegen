package com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitiveAndArgs.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  string: () -> String? = stringDefault,
) {
  private val __string: () -> String? = string

  @get:JvmName("getString")
  public val string: String?
    get() = __string.invoke()

  public companion object {
    private val stringDefault: () -> String? = 
        { throw IllegalStateException("Field `string` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var string: () -> String? = stringDefault

    @JsonProperty("string")
    public fun withString(string: String?): Builder = this.apply {
      this.string = { string }
    }

    public fun build(): Query = Query(
      string = string,
    )
  }
}
