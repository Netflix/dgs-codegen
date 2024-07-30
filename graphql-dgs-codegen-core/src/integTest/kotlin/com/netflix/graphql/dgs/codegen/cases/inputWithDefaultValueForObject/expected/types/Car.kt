package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForObject.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class Car @JsonCreator constructor(
  @JsonProperty("brand")
  public val brand: String = default<Car, String>("brand", "BMW"),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("brand" to brand)
}
