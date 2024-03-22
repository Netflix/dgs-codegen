package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableProperties.expected

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableProperties.expected.client.QueryProjection
import graphql.language.OperationDefinition
import kotlin.String

public object DgsClient {
    public fun buildQuery(_projection: QueryProjection.() -> QueryProjection): String =
        GraphQLProjection.asQuery(OperationDefinition.Operation.QUERY, QueryProjection(), _projection)
}
