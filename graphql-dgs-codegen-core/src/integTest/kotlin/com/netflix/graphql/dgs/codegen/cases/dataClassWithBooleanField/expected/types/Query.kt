package com.netflix.graphql.dgs.codegen.cases.dataClassWithBooleanField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  test: () -> RequiredTestType? = testDefault,
) {
  private val _test: () -> RequiredTestType? = test

  @get:JvmName("getTest")
  public val test: RequiredTestType?
    get() = _test.invoke()

  public companion object {
    private val testDefault: () -> RequiredTestType? = 
        { throw IllegalStateException("Field `test` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var test: () -> RequiredTestType? = testDefault

    @JsonProperty("test")
    public fun withTest(test: RequiredTestType?): Builder = this.apply {
      this.test = { test }
    }

    public fun build() = Query(
      test = test,
    )
  }
}
