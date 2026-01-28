package com.netflix.graphql.dgs.codegen.cases.dataClassWithBooleanField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import java.lang.IllegalStateException
import kotlin.Boolean
import kotlin.jvm.JvmName
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize as FasterxmlJacksonDatabindAnnotationJsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder as FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
import tools.jackson.databind.`annotation`.JsonDeserialize as ToolsJacksonDatabindAnnotationJsonDeserialize
import tools.jackson.databind.`annotation`.JsonPOJOBuilder as ToolsJacksonDatabindAnnotationJsonPOJOBuilder

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = RequiredTestType.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = RequiredTestType.Builder::class)
public class RequiredTestType(
  isRequired: () -> Boolean = isRequiredDefault,
) {
  private val __isRequired: () -> Boolean = isRequired

  @get:JvmName("getIsRequired")
  public val isRequired: Boolean
    get() = __isRequired.invoke()

  public companion object {
    private val isRequiredDefault: () -> Boolean = 
        { throw IllegalStateException("Field `isRequired` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
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
