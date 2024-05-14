package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultEnumValueForArray.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class SomeType @JsonCreator constructor(
  @JsonProperty("colors")
  public val colors: List<Color?>? = default<SomeType, List<Color?>?>("colors"),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("colors" to colors)
}
