package com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.Generated
import com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.types.MovieFilter
import kotlin.String

@Generated
public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun search(
    movieFilter: MovieFilter,
    _alias: String? = null,
    _projection: MovieProjection.() -> MovieProjection,
  ): QueryProjection {
    field(_alias, "search", MovieProjection(inputValueSerializer), _projection, "movieFilter" to
        movieFilter)
    return this
  }
}
