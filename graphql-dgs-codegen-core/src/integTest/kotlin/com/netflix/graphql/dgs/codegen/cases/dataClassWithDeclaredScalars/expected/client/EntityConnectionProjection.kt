package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class EntityConnectionProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun pageInfo(_projection: PageInfoProjection.() -> PageInfoProjection):
      EntityConnectionProjection {
    field("pageInfo", PageInfoProjection(inputValueSerializer), _projection)
    return this
  }

  public fun edges(_projection: EntityEdgeProjection.() -> EntityEdgeProjection):
      EntityConnectionProjection {
    field("edges", EntityEdgeProjection(inputValueSerializer), _projection)
    return this
  }
}
