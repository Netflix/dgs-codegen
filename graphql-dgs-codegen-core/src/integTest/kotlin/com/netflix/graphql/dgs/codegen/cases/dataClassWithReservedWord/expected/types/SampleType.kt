package com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.jvm.JvmName
import com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = SampleType.Builder::class)
public class SampleType(
  `return`: () -> String = returnDefault,
) {
  private val __return: () -> String = `return`

  @get:JvmName("getReturn")
  public val `return`: String
    get() = __return.invoke()

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  public companion object {
    private val returnDefault: () -> String = 
        { throw IllegalStateException("Field `return` was not requested") }
  }

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
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
