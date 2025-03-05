package com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitiveAndArgs.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public data class I @JsonCreator constructor(
  @JsonProperty("arg")
  public val arg: String? = default<I, String?>("arg", null),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("arg" to arg)
}
