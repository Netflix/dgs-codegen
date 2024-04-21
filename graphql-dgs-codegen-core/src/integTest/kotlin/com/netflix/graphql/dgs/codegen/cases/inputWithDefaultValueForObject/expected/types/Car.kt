package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForObject.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class Car(
  public val brand: String,
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("brand" to brand)
}
