package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultEnumValueForArray.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class SomeType(
  public val colors: List<Color?>? = default("colors"),
) : GraphQLInput() {
  public override fun fields(): List<Pair<String, Any?>> = listOf("colors" to colors)
}
