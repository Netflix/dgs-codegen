package com.netflix.graphql.dgs.codegen.cases.dataClassWithBooleanField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithBooleanField.expected.Generated
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
  test: () -> RequiredTestType? = testDefault,
) {
  private val __test: () -> RequiredTestType? = test

  @get:JvmName("getTest")
  public val test: RequiredTestType?
    get() = __test.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__test === testDefault) testDefault else test,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Query &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__test === testDefault) null else "test=" + test,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Query(", postfix =
      ")")

  @Generated
  public companion object {
    private val testDefault: () -> RequiredTestType? = 
        { throw IllegalStateException("Field `test` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
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
