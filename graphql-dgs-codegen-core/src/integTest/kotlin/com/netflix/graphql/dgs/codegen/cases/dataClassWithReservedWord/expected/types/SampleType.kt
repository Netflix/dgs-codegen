package com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = SampleType.Builder::class)
public class SampleType(
  `return`: () -> String = returnDefault,
) {
  private val __return: () -> String = `return`

  @get:JvmName("getReturn")
  public val `return`: String
    get() = __return.invoke()

  public companion object {
    private val returnDefault: () -> String = 
        { throw IllegalStateException("Field `return` was not requested") }
  }

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
