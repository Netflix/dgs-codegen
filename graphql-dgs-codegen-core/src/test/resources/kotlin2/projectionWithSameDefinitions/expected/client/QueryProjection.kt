package kotlin2.projectionWithSameDefinitions.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun shows(_projection: ShowProjection.() -> ShowProjection): QueryProjection {
    project("shows", ShowProjection(), _projection)
    return this
  }
}
