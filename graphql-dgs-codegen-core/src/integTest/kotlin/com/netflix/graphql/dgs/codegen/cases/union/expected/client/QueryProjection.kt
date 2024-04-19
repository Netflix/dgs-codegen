package com.netflix.graphql.dgs.codegen.cases.union.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun search(_alias: String? = null,
      _projection: SearchResultProjection.() -> SearchResultProjection): QueryProjection {
    field(_alias, "search", SearchResultProjection(), _projection)
    return this
  }
}
