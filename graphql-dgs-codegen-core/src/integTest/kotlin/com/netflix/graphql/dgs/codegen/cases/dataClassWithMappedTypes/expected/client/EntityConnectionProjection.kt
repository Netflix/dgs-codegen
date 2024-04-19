package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class EntityConnectionProjection : GraphQLProjection() {
  public fun pageInfo(_alias: String? = null,
      _projection: PageInfoProjection.() -> PageInfoProjection): EntityConnectionProjection {
    field(_alias, "pageInfo", PageInfoProjection(), _projection)
    return this
  }

  public fun edges(_alias: String? = null,
      _projection: EntityEdgeProjection.() -> EntityEdgeProjection): EntityConnectionProjection {
    field(_alias, "edges", EntityEdgeProjection(), _projection)
    return this
  }
}
