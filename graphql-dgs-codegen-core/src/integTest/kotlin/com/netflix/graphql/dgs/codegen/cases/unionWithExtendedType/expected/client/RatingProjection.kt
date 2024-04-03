package com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class RatingProjection : GraphQLProjection() {
  public val stars: RatingProjection
    get() {
      field("stars")
      return this
    }
}
