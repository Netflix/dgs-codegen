package kotlin2.dataClassWithMappedTypes.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class EntityConnectionProjection : GraphQLProjection() {
  public fun pageInfo(_projection: PageInfoProjection.() -> PageInfoProjection):
      EntityConnectionProjection {
    project("pageInfo", PageInfoProjection(), _projection)
    return this
  }

  public fun edges(_projection: EntityEdgeProjection.() -> EntityEdgeProjection):
      EntityConnectionProjection {
    project("edges", EntityEdgeProjection(), _projection)
    return this
  }
}
