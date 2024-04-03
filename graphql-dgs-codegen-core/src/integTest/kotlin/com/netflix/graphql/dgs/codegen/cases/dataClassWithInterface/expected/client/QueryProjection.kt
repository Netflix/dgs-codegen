package com.netflix.graphql.dgs.codegen.cases.dataClassWithInterface.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun people(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    field("people", PersonProjection(), _projection)
    return this
  }
}
