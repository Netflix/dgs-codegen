package com.netflix.graphql.dgs.codegen.cases.union.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class MovieProjection : GraphQLProjection() {
  public val title: MovieProjection
    get() {
      field("title")
      return this
    }
}
