package com.netflix.graphql.dgs.codegen.cases.enumWithExtendedType.expected

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.enumWithExtendedType.expected.client.QueryProjection
import graphql.language.OperationDefinition
import kotlin.String

public object DgsClient {
  public fun buildQuery(inputValueSerializer: InputValueSerializerInterface? = null,
      _projection: QueryProjection.() -> QueryProjection): String =
      GraphQLProjection.asQuery(OperationDefinition.Operation.QUERY,
      QueryProjection(inputValueSerializer), _projection)
}
