package kotlin2.dataClassWithMappedTypes.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class EntityEdgeProjection : GraphQLProjection() {
  public val cursor: EntityEdgeProjection
    get() {
      field("cursor")
      return this
    }

  public fun node(_projection: EntityProjection.() -> EntityProjection): EntityEdgeProjection {
    project("node", EntityProjection(), _projection)
    return this
  }
}
