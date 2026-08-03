package com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmName

@Generated
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Query) return false
    return (__q1 === q1Default) == (other.__q1 === q1Default) && (__q1 === q1Default ||
        Objects.equals(q1, other.q1)) &&
    (__q2 === q2Default) == (other.__q2 === q2Default) && (__q2 === q2Default || Objects.equals(q2,
        other.q2))
  }

  override fun hashCode(): Int = Objects.hash(if (__q1 === q1Default) q1Default else q1,
  if (__q2 === q2Default) q2Default else q2)

  override fun toString(): String = listOfNotNull(if (__q1 === q1Default) null else "q1=" + q1, if
      (__q2 === q2Default) null else "q2=" + q2).joinToString(prefix = "Query(", postfix = ")")

  @Generated
  public companion object {
    private val q1Default: () -> String? = 
        { throw IllegalStateException("Field `q1` was not requested") }

    private val q2Default: () -> String? = 
        { throw IllegalStateException("Field `q2` was not requested") }
  }

  @Generated
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
