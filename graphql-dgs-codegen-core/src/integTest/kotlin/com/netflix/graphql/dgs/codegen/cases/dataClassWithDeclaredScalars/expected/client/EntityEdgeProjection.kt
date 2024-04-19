package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class EntityEdgeProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val cursor: EntityEdgeProjection
    get() {
      field("cursor")
      return this
    }

  public fun node(_projection: EntityProjection.() -> EntityProjection): EntityEdgeProjection {
    field("node", EntityProjection(inputValueSerializer), _projection)
    return this
  }
}
