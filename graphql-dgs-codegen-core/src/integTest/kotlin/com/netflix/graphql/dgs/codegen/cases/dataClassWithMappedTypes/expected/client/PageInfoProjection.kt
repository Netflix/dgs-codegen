package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class PageInfoProjection : GraphQLProjection() {
    public val startCursor: PageInfoProjection
        get() {
            field("startCursor")
            return this
        }

    public val endCursor: PageInfoProjection
        get() {
            field("endCursor")
            return this
        }

    public val hasNextPage: PageInfoProjection
        get() {
            field("hasNextPage")
            return this
        }

    public val hasPreviousPage: PageInfoProjection
        get() {
            field("hasPreviousPage")
            return this
        }
}
