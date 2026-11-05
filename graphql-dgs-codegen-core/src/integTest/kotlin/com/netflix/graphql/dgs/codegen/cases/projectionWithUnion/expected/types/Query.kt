package com.netflix.graphql.dgs.codegen.cases.projectionWithUnion.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.projectionWithUnion.expected.Generated
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
  u: () -> U? = uDefault,
  us: () -> List<U?>? = usDefault,
) {
  private val __u: () -> U? = u

  private val __us: () -> List<U?>? = us

  @get:JvmName("getU")
  public val u: U?
    get() = __u.invoke()

  @get:JvmName("getUs")
  public val us: List<U?>?
    get() = __us.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__u === uDefault) uDefault else u,
      if (__us === usDefault) usDefault else us,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Query &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__u === uDefault) null else "u=" + u,
      if (__us === usDefault) null else "us=" + us,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Query(", postfix =
      ")")

  @Generated
  public companion object {
    private val uDefault: () -> U? = 
        { throw IllegalStateException("Field `u` was not requested") }

    private val usDefault: () -> List<U?>? = 
        { throw IllegalStateException("Field `us` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var u: () -> U? = uDefault

    private var us: () -> List<U?>? = usDefault

    @JsonProperty("u")
    public fun withU(u: U?): Builder = this.apply {
      this.u = { u }
    }

    @JsonProperty("us")
    public fun withUs(us: List<U?>?): Builder = this.apply {
      this.us = { us }
    }

    public fun build(): Query = Query(
      u = u,
      us = us,
    )
  }
}
