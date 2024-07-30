package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultBigDecimal.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import java.math.BigDecimal
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class OrderFilter @JsonCreator constructor(
  @JsonProperty("min")
  public val min: BigDecimal = default<OrderFilter, BigDecimal>("min", java.math.BigDecimal("1.1")),
  @JsonProperty("avg")
  public val avg: BigDecimal = default<OrderFilter, BigDecimal>("avg", java.math.BigDecimal(1.12)),
  @JsonProperty("max")
  public val max: BigDecimal = default<OrderFilter, BigDecimal>("max",
      java.math.BigDecimal(3.14E+19)),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("min" to min, "avg" to avg, "max" to max)
}
