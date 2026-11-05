package com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected.Generated
import java.lang.IllegalStateException
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = SampleType.Builder::class)
public class SampleType(
  `return`: () -> String = returnDefault,
) {
  private val __return: () -> String = `return`

  @get:JvmName("getReturn")
  public val `return`: String
    get() = __return.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__return === returnDefault) returnDefault else `return`,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is SampleType &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__return === returnDefault) null else "return=" + `return`,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "SampleType(", postfix
      = ")")

  @Generated
  public companion object {
    private val returnDefault: () -> String = 
        { throw IllegalStateException("Field `return` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var `return`: () -> String = returnDefault

    @JsonProperty("return")
    public fun withReturn(`return`: String): Builder = this.apply {
      this.`return` = { `return` }
    }

    public fun build(): SampleType = SampleType(
      `return` = `return`,
    )
  }
}
