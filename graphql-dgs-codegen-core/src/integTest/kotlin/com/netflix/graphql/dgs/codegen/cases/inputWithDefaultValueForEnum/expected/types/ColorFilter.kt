package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForEnum.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class ColorFilter @JsonCreator constructor(
  @JsonProperty("color")
  public val color: Color? = default<ColorFilter, Color?>("color"),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("color" to color)
}
