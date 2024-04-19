package com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun `is`(_projection: IProjection.() -> IProjection): QueryProjection {
    field("is", IProjection(), _projection)
    return this
  }
}
