package com.netflix.graphql.dgs.codegen.cases.union.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class ActorProjection : GraphQLProjection() {
  public val name: ActorProjection
    get() {
      field("name")
      return this
    }
}
