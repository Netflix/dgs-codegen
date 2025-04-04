package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultCurrency.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import java.util.Currency
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public data class OrderFilter @JsonCreator constructor(
  @JsonProperty("value")
  public val `value`: Currency = default<OrderFilter, Currency>("value",
      java.util.Currency.getInstance("USD")),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("value" to `value`)
}
