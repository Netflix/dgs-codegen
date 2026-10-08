package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultMap.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultMap.expected.Generated
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultMap.expected.types.MovieFilter

@Generated
public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun movies(filter: MovieFilter? = default<QueryProjection, MovieFilter?>("filter")):
      QueryProjection {
    field("movies", "filter" to filter)
    return this
  }
}
