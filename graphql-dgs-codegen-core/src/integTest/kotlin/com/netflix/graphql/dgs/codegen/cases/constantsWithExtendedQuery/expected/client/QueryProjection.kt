package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedQuery.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
    public fun people(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
        field("people", PersonProjection(), _projection)
        return this
    }

    public fun friends(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
        field("friends", PersonProjection(), _projection)
        return this
    }
}
