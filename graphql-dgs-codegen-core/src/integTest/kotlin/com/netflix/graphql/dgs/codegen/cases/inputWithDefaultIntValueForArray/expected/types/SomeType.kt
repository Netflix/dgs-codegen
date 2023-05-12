package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultIntValueForArray.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class SomeType(
    public val numbers: List<Int?>? = default<SomeType, List<Int?>?>("numbers")
) : GraphQLInput() {
    public override fun fields(): List<Pair<String, Any?>> = listOf("numbers" to numbers)
}
