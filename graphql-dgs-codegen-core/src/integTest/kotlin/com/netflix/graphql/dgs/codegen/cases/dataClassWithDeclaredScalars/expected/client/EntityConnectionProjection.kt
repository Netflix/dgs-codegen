package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class EntityConnectionProjection : GraphQLProjection() {
  public fun pageInfo(_projection: PageInfoProjection.() -> PageInfoProjection):
      EntityConnectionProjection {
    field("pageInfo", PageInfoProjection(), _projection)
    return this
  }

  public fun edges(_projection: EntityEdgeProjection.() -> EntityEdgeProjection):
      EntityConnectionProjection {
    field("edges", EntityEdgeProjection(), _projection)
    return this
  }
}
