package com.netflix.graphql.dgs.codegen.cases.dataClassWithBooleanField.expected.types

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
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = Query.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = Query.Builder::class)
public class Query(
  test: () -> RequiredTestType? = testDefault,
) {
  private val __test: () -> RequiredTestType? = test

  @get:JvmName("getTest")
  public val test: RequiredTestType?
    get() = __test.invoke()

  public companion object {
    private val testDefault: () -> RequiredTestType? = 
        { throw IllegalStateException("Field `test` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var test: () -> RequiredTestType? = testDefault

    @JsonProperty("test")
    public fun withTest(test: RequiredTestType?): Builder = this.apply {
      this.test = { test }
    }

    public fun build(): Query = Query(
      test = test,
    )
  }
}
