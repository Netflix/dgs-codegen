package com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.Generated
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
  me: () -> Person? = meDefault,
) {
  private val __me: () -> Person? = me

  @get:JvmName("getMe")
  public val me: Person?
    get() = __me.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__me === meDefault) meDefault else me,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Query &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__me === meDefault) null else "me=" + me,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Query(", postfix =
      ")")

  @Generated
  public companion object {
    private val meDefault: () -> Person? = 
        { throw IllegalStateException("Field `me` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var me: () -> Person? = meDefault

    @JsonProperty("me")
    public fun withMe(me: Person?): Builder = this.apply {
      this.me = { me }
    }

    public fun build(): Query = Query(
      me = me,
    )
  }
}
