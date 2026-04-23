package com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.collections.List
import kotlin.jvm.JvmName
import com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  `is`: () -> List<I?>? = isDefault,
) {
  private val __is: () -> List<I?>? = `is`

  @get:JvmName("getIs")
  public val `is`: List<I?>?
    get() = __is.invoke()

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  public companion object {
    private val isDefault: () -> List<I?>? = 
        { throw IllegalStateException("Field `is` was not requested") }
  }

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var `is`: () -> List<I?>? = isDefault

    @JsonProperty("is")
    public fun withIs(`is`: List<I?>?): Builder = this.apply {
      this.`is` = { `is` }
    }

    public fun build(): Query = Query(
      `is` = `is`,
    )
  }
}
