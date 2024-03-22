package com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class I1(
    public val arg1: I1? = default<I1, I1?>("arg1"),
    public val arg2: I2? = default<I1, I2?>("arg2")
) : GraphQLInput() {
    override fun fields(): List<Pair<String, Any?>> = listOf("arg1" to arg1, "arg2" to arg2)
}
