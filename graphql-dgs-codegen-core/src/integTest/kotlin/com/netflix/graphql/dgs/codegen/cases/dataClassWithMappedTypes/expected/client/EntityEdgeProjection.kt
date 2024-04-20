package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class EntityEdgeProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val cursor: EntityEdgeProjection
    get() {
      field("cursor")
      return this
    }

  public fun node(_alias: String? = null, _projection: EntityProjection.() -> EntityProjection):
      EntityEdgeProjection {
    field(_alias, "node", EntityProjection(inputValueSerializer), _projection)
    return this
  }
}
