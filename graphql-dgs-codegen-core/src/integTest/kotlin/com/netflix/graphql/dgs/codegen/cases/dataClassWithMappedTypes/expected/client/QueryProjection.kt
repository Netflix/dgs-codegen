package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun entity(_alias: String? = null, _projection: EntityProjection.() -> EntityProjection):
      QueryProjection {
    field(_alias, "entity", EntityProjection(), _projection)
    return this
  }

  public fun entityConnection(_alias: String? = null,
      _projection: EntityConnectionProjection.() -> EntityConnectionProjection): QueryProjection {
    field(_alias, "entityConnection", EntityConnectionProjection(), _projection)
    return this
  }
}
