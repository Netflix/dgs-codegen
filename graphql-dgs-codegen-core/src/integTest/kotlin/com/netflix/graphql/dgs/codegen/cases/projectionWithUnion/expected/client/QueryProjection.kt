package com.netflix.graphql.dgs.codegen.cases.projectionWithUnion.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun u(_projection: UProjection.() -> UProjection): QueryProjection {
    field("u", UProjection(), _projection)
    return this
  }

  public fun us(_projection: UProjection.() -> UProjection): QueryProjection {
    field("us", UProjection(), _projection)
    return this
  }
}
