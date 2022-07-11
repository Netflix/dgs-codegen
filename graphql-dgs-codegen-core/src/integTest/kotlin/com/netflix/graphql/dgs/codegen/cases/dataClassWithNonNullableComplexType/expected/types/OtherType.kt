package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableComplexType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = OtherType.Builder::class)
public class OtherType(
  name: () -> String = nameDefault,
) {
  private val _name: () -> String = name

  public val name: String
    get() = _name.invoke()

  public companion object {
    private val nameDefault: () -> String = 
        { throw IllegalStateException("Field `name` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var name: () -> String = nameDefault

    @JsonProperty("name")
    public fun withName(name: String): Builder = this.apply {
      this.name = { name }
    }

    public fun build() = OtherType(
      name = name,
    )
  }
}
