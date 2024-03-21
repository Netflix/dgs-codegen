package com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class SampleTypeProjection : GraphQLProjection() {
    public val `return`: SampleTypeProjection
        get() {
            field("return")
            return this
        }
}
