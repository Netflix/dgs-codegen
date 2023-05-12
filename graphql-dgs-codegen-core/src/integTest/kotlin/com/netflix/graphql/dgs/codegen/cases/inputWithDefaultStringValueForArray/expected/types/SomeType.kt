package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultStringValueForArray.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class SomeType(
    public val names: List<String?>? = default<SomeType, List<String?>?>("names")
) : GraphQLInput() {
    public override fun fields(): List<Pair<String, Any?>> = listOf("names" to names)
}
