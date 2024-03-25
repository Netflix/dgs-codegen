package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun entity(_projection: EntityProjection.() -> EntityProjection): QueryProjection {
    field("entity", EntityProjection(), _projection)
    return this
  }

  public
      fun entityConnection(_projection: EntityConnectionProjection.() -> EntityConnectionProjection):
      QueryProjection {
    field("entityConnection", EntityConnectionProjection(), _projection)
    return this
  }
}
