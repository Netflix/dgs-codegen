package com.netflix.graphql.dgs.codegen.cases.`enum`.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
    public val types: QueryProjection
        get() {
            field("types")
            return this
        }
}
