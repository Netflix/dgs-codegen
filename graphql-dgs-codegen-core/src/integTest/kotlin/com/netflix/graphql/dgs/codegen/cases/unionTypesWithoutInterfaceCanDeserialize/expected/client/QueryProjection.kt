package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun search(text: String,
      _projection: SearchResultPageProjection.() -> SearchResultPageProjection): QueryProjection {
    field("search", SearchResultPageProjection(), _projection, "text" to text)
    return this
  }
}
