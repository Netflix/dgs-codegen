package com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun me(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    field("me", PersonProjection(), _projection)
    return this
  }
}
