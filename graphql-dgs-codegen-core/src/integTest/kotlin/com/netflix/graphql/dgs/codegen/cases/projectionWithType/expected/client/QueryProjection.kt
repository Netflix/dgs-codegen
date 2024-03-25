package com.netflix.graphql.dgs.codegen.cases.projectionWithType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun person(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    field("person", PersonProjection(), _projection)
    return this
  }

  public fun people(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    field("people", PersonProjection(), _projection)
    return this
  }
}
