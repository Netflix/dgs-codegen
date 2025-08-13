package com.netflix.graphql.dgs.codegen.cases.inputWithPrefix.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.inputWithPrefix.expected.types.DgsMovieFilter

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun movies(filter: DgsMovieFilter? = default<QueryProjection, DgsMovieFilter?>("filter")):
      QueryProjection {
    field("movies", "filter" to filter)
    return this
  }
}
