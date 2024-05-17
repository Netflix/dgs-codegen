package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultStringValueForArray.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class SomeType @JsonCreator constructor(
  @JsonProperty("names")
  public val names: List<String?>? = default<SomeType, List<String?>?>("names", listOf("A", "B")),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("names" to names)
}
