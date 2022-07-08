package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultIntValueForArray.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Int
import kotlin.collections.List

public class SomeType(
  public val numbers: List<Int?>? = default("numbers"),
) : GraphQLInput()
