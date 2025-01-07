package com.netflix.graphql.dgs.codegen.cases.dataClassWithPrefix.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.dataClassWithPrefix.expected.types.DgsMovieFilter
import kotlin.String

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun search(
    movieFilter: DgsMovieFilter,
    _alias: String? = null,
    _projection: MovieProjection.() -> MovieProjection,
  ): QueryProjection {
    field(_alias, "search", MovieProjection(inputValueSerializer), _projection, "movieFilter" to
        movieFilter)
    return this
  }
}
