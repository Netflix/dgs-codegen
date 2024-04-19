package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun entity(_projection: EntityProjection.() -> EntityProjection): QueryProjection {
    field("entity", EntityProjection(inputValueSerializer), _projection)
    return this
  }

  public
      fun entityConnection(_projection: EntityConnectionProjection.() -> EntityConnectionProjection):
      QueryProjection {
    field("entityConnection", EntityConnectionProjection(inputValueSerializer), _projection)
    return this
  }
}
