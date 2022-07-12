package com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun search(_projection: SearchResultProjection.() -> SearchResultProjection):
      QueryProjection {
    field("search", SearchResultProjection(), _projection)
    return this
  }
}
