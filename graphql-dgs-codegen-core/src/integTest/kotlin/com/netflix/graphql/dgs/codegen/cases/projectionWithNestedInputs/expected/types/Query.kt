package com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.types

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
  q1: () -> String? = q1Default,
  q2: () -> String? = q2Default,
) {
  private val __q1: () -> String? = q1

  private val __q2: () -> String? = q2

  @get:JvmName("getQ1")
  public val q1: String?
    get() = __q1.invoke()

  @get:JvmName("getQ2")
  public val q2: String?
    get() = __q2.invoke()

  public companion object {
    private val q1Default: () -> String? = 
        { throw IllegalStateException("Field `q1` was not requested") }

    private val q2Default: () -> String? = 
        { throw IllegalStateException("Field `q2` was not requested") }
  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var q1: () -> String? = q1Default

    private var q2: () -> String? = q2Default

    @JsonProperty("q1")
    public fun withQ1(q1: String?): Builder = this.apply {
      this.q1 = { q1 }
    }

    @JsonProperty("q2")
    public fun withQ2(q2: String?): Builder = this.apply {
      this.q2 = { q2 }
    }

    public fun build(): Query = Query(
      q1 = q1,
      q2 = q2,
    )
  }
}
