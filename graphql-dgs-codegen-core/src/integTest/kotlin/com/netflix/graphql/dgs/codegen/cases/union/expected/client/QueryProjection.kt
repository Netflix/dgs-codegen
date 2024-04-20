package com.netflix.graphql.dgs.codegen.cases.union.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun search(_alias: String? = null,
      _projection: SearchResultProjection.() -> SearchResultProjection): QueryProjection {
    field(_alias, "search", SearchResultProjection(inputValueSerializer), _projection)
    return this
  }
}
