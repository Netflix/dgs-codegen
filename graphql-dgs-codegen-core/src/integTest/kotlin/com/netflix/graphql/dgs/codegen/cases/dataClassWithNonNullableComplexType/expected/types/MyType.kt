package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableComplexType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import java.lang.IllegalStateException
import kotlin.jvm.JvmName
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize as FasterxmlJacksonDatabindAnnotationJsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder as FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
import tools.jackson.databind.`annotation`.JsonDeserialize as ToolsJacksonDatabindAnnotationJsonDeserialize
import tools.jackson.databind.`annotation`.JsonPOJOBuilder as ToolsJacksonDatabindAnnotationJsonPOJOBuilder

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = MyType.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = MyType.Builder::class)
public class MyType(
  other: () -> OtherType = otherDefault,
) {
  private val __other: () -> OtherType = other

  @get:JvmName("getOther")
  public val other: OtherType
    get() = __other.invoke()

  public companion object {
    private val otherDefault: () -> OtherType = 
        { throw IllegalStateException("Field `other` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
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
