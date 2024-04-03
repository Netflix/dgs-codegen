package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableComplexType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class MyTypeProjection : GraphQLProjection() {
    public fun other(_projection: OtherTypeProjection.() -> OtherTypeProjection): MyTypeProjection {
        field("other", OtherTypeProjection(), _projection)
        return this
    }
}
