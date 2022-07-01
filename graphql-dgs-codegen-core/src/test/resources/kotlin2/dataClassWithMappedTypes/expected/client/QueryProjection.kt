package kotlin2.dataClassWithMappedTypes.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun entity(_projection: EntityProjection.() -> EntityProjection): QueryProjection {
    project("entity", EntityProjection(), _projection)
    return this
  }

  public
      fun entityConnection(_projection: EntityConnectionProjection.() -> EntityConnectionProjection):
      QueryProjection {
    project("entityConnection", EntityConnectionProjection(), _projection)
    return this
  }
}
