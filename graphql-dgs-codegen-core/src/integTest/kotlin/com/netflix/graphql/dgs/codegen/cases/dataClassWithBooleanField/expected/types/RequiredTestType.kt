package com.netflix.graphql.dgs.codegen.cases.dataClassWithBooleanField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithBooleanField.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = RequiredTestType.Builder::class)
public class RequiredTestType(
  isRequired: () -> Boolean = isRequiredDefault,
) {
  private val __isRequired: () -> Boolean = isRequired

  @get:JvmName("getIsRequired")
  public val isRequired: Boolean
    get() = __isRequired.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is RequiredTestType) return false
    return (__isRequired === isRequiredDefault) == (other.__isRequired === isRequiredDefault) &&
        (__isRequired === isRequiredDefault || Objects.equals(isRequired, other.isRequired))
  }

  override fun hashCode(): Int = Objects.hash(if (__isRequired === isRequiredDefault)
      isRequiredDefault else isRequired)

  override fun toString(): String = listOfNotNull(if (__isRequired === isRequiredDefault) null else
      "isRequired=" + isRequired).joinToString(prefix = "RequiredTestType(", postfix = ")")

  @Generated
  public companion object {
    private val isRequiredDefault: () -> Boolean = 
        { throw IllegalStateException("Field `isRequired` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var isRequired: () -> Boolean = isRequiredDefault

    @JsonProperty("isRequired")
    public fun withIsRequired(isRequired: Boolean): Builder = this.apply {
      this.isRequired = { isRequired }
    }

    public fun build(): RequiredTestType = RequiredTestType(
      isRequired = isRequired,
    )
  }
}
