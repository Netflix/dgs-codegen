package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableComplexType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableComplexType.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MyType) return false
    return (__other === otherDefault) == (other.__other === otherDefault) && (__other ===
        otherDefault || Objects.equals(other, other.other))
  }

  override fun hashCode(): Int = Objects.hash(if (__other === otherDefault) otherDefault else other)

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
