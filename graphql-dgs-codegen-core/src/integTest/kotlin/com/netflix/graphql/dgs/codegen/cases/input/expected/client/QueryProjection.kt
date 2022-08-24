package com.netflix.graphql.dgs.codegen.cases.input.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.input.expected.types.MovieFilter

public class QueryProjection : GraphQLProjection() {
  public fun movies(filter: MovieFilter? = default<QueryProjection, MovieFilter?>("filter")):
      QueryProjection {
    field("movies", "filter" to filter)
    return this
  }
}
