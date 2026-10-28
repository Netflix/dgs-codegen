package com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitives.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitives.expected.Generated
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
  strings: () -> List<String?>? = stringsDefault,
) {
  private val __string: () -> String? = string

  private val __strings: () -> List<String?>? = strings

  @get:JvmName("getString")
  public val string: String?
    get() = __string.invoke()

  @get:JvmName("getStrings")
  public val strings: List<String?>?
    get() = __strings.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__string === stringDefault) stringDefault else string,
      if (__strings === stringsDefault) stringsDefault else strings,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Query &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__string === stringDefault) null else "string=" + string,
      if (__strings === stringsDefault) null else "strings=" + strings,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Query(", postfix =
      ")")

  @Generated
  public companion object {
    private val stringDefault: () -> String? = 
        { throw IllegalStateException("Field `string` was not requested") }

    private val stringsDefault: () -> List<String?>? = 
        { throw IllegalStateException("Field `strings` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var string: () -> String? = stringDefault

    private var strings: () -> List<String?>? = stringsDefault

    @JsonProperty("string")
    public fun withString(string: String?): Builder = this.apply {
      this.string = { string }
    }

    @JsonProperty("strings")
    public fun withStrings(strings: List<String?>?): Builder = this.apply {
      this.strings = { strings }
    }

    public fun build(): Query = Query(
      string = string,
      strings = strings,
    )
  }
}
