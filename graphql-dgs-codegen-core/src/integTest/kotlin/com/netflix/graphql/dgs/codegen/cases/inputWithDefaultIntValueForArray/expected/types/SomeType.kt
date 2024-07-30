package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultIntValueForArray.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class SomeType @JsonCreator constructor(
  @JsonProperty("numbers")
  public val numbers: List<Int?>? = default<SomeType, List<Int?>?>("numbers", listOf(1, 2, 3)),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("numbers" to numbers)
}
