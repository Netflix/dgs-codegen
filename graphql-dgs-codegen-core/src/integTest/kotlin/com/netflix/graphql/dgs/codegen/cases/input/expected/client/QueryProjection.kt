package com.netflix.graphql.dgs.codegen.cases.input.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.input.expected.types.MovieFilter

public class QueryProjection : GraphQLProjection() {
  public fun movies(filter: MovieFilter? = default("filter")): QueryProjection {
    val args = formatArgs("filter" to filter)
    field("movies($args)")
    return this
  }
}
