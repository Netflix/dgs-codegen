package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForArray.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public data class SomeType @JsonCreator constructor(
  @JsonProperty("names")
  public val names: List<String?>? = default<SomeType, List<String?>?>("names", emptyList()),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("names" to names)
}
