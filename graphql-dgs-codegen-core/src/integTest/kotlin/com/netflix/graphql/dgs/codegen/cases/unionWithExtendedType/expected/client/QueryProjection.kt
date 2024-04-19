package com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun search(_projection: SearchResultProjection.() -> SearchResultProjection):
      QueryProjection {
    field("search", SearchResultProjection(inputValueSerializer), _projection)
    return this
  }
}
