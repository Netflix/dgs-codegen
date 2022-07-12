package com.netflix.graphql.dgs.codegen.cases.union.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun search(_projection: SearchResultProjection.() -> SearchResultProjection):
      QueryProjection {
    field("search", SearchResultProjection(), _projection)
    return this
  }
}
