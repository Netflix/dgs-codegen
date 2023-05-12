package com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.client.QueryProjection
import graphql.language.OperationDefinition
import kotlin.String

public object DgsClient {
    public fun buildQuery(_projection: QueryProjection.() -> QueryProjection): String =
        GraphQLProjection.asQuery(OperationDefinition.Operation.QUERY, QueryProjection(), _projection)
}
