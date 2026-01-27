package com.netflix.graphql.dgs.codegen.cases.inputWithReservedWord.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public data class SampleInput(
  @JsonProperty("return")
  public val `return`: String,
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("return" to `return`)
}
