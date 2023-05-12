package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
    public fun products(_projection: ProductProjection.() -> ProductProjection): QueryProjection {
        field("products", ProductProjection(), _projection)
        return this
    }
}
