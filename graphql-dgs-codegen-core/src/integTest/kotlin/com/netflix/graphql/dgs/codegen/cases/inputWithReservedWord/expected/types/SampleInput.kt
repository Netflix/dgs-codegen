package com.netflix.graphql.dgs.codegen.cases.inputWithReservedWord.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import com.netflix.graphql.dgs.codegen.cases.inputWithReservedWord.expected.Generated
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

@Generated
public data class SampleInput(
  @JsonProperty("return")
  public val `return`: String,
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("return" to `return`)
}
