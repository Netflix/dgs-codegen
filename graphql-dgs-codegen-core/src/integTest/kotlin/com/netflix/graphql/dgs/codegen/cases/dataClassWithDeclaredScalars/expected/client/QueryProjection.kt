package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun entity(_alias: String? = null, _projection: EntityProjection.() -> EntityProjection):
      QueryProjection {
    field(_alias, "entity", EntityProjection(inputValueSerializer), _projection)
    return this
  }

  public fun entityConnection(_alias: String? = null,
      _projection: EntityConnectionProjection.() -> EntityConnectionProjection): QueryProjection {
    field(_alias, "entityConnection", EntityConnectionProjection(inputValueSerializer), _projection)
    return this
  }
}
