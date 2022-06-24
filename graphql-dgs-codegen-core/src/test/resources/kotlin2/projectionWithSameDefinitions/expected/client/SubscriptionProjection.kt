package kotlin2.projectionWithSameDefinitions.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class SubscriptionProjection : GraphQLProjection(defaultFields = emptySet()) {
  public fun shows(_projection: ShowProjection.() -> ShowProjection): SubscriptionProjection {
    project("shows", ShowProjection(), _projection)
    return this
  }
}
