package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultEnumValueForArray.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultEnumValueForArray.expected.Generated
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

@Generated
public data class SomeType(
  @JsonProperty("colors")
  public val colors: List<Color?>? = default<SomeType, List<Color?>?>("colors",
      listOf(com.netflix.graphql.dgs.codegen.cases.inputWithDefaultEnumValueForArray.expected.types.Color.red)),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("colors" to colors)
}
