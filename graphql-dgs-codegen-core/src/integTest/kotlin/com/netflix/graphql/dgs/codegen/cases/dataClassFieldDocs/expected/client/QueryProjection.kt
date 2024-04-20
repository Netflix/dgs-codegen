package com.netflix.graphql.dgs.codegen.cases.dataClassFieldDocs.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.dataClassFieldDocs.expected.types.MovieFilter
import kotlin.String

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun search(
    _alias: String? = null,
    movieFilter: MovieFilter,
    _projection: MovieProjection.() -> MovieProjection,
  ): QueryProjection {
    field(_alias, "search", MovieProjection(inputValueSerializer), _projection, "movieFilter" to
        movieFilter)
    return this
  }
}
