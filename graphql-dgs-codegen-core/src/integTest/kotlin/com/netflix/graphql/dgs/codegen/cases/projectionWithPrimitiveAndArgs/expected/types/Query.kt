package com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitiveAndArgs.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitiveAndArgs.expected.Generated
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
  string: () -> String? = stringDefault,
) {
  private val __string: () -> String? = string

  @get:JvmName("getString")
  public val string: String?
    get() = __string.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__string === stringDefault) stringDefault else string,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Query &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__string === stringDefault) null else "string=" + string,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Query(", postfix =
      ")")

  @Generated
  public companion object {
    private val stringDefault: () -> String? = 
        { throw IllegalStateException("Field `string` was not requested") }
  }

  @Generated
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
