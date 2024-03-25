package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class EntityEdgeProjection : GraphQLProjection() {
  public val cursor: EntityEdgeProjection
    get() {
      field("cursor")
      return this
    }

  public fun node(_projection: EntityProjection.() -> EntityProjection): EntityEdgeProjection {
    field("node", EntityProjection(), _projection)
    return this
  }
}
