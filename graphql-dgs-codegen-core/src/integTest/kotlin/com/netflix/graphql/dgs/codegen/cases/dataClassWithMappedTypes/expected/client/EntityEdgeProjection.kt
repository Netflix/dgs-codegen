package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class EntityEdgeProjection : GraphQLProjection() {
  public val cursor: EntityEdgeProjection
    get() {
      field("cursor")
      return this
    }

  public fun node(_alias: String? = null, _projection: EntityProjection.() -> EntityProjection):
      EntityEdgeProjection {
    field(_alias, "node", EntityProjection(), _projection)
    return this
  }
}
