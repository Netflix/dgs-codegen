package com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.types.MovieFilter

public class QueryProjection : GraphQLProjection() {
  public fun movies(filter: MovieFilter? = default("filter")): QueryProjection {
    val args = formatArgs("filter" to filter)
    field("movies($args)")
    return this
  }
}
