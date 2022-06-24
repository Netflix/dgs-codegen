package kotlin2.projectionWithSameDefinitions.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class MutationProjection : GraphQLProjection() {
  public fun shows(_projection: ShowProjection.() -> ShowProjection): MutationProjection {
    project("shows", ShowProjection(), _projection)
    return this
  }
}
