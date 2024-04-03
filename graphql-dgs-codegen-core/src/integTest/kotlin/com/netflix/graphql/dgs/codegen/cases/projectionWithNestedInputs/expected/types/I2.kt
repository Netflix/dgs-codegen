package com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class I2(
    public val arg1: String? = default<I2, String?>("arg1"),
    public val arg2: String? = default<I2, String?>("arg2")
) : GraphQLInput() {
    override fun fields(): List<Pair<String, Any?>> = listOf("arg1" to arg1, "arg2" to arg2)
}
