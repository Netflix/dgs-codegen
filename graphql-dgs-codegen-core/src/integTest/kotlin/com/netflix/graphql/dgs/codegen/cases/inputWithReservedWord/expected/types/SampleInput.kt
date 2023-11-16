package com.netflix.graphql.dgs.codegen.cases.inputWithReservedWord.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class SampleInput(
  public val `return`: String,
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("return" to `return`)
}
