package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableComplexType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableComplexType.expected.Generated
import java.lang.IllegalStateException
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = MyType.Builder::class)
public class MyType(
  other: () -> OtherType = otherDefault,
) {
  private val __other: () -> OtherType = other

  @get:JvmName("getOther")
  public val other: OtherType
    get() = __other.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__other === otherDefault) otherDefault else other,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is MyType &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__other === otherDefault) null else "other=" + other,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "MyType(", postfix =
      ")")

  @Generated
  public companion object {
    private val otherDefault: () -> OtherType = 
        { throw IllegalStateException("Field `other` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var other: () -> OtherType = otherDefault

    @JsonProperty("other")
    public fun withOther(other: OtherType): Builder = this.apply {
      this.other = { other }
    }

    public fun build(): MyType = MyType(
      other = other,
    )
  }
}
