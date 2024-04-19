package com.netflix.graphql.dgs.codegen.cases.dataClassFieldDocs.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.dataClassFieldDocs.expected.types.MovieFilter
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun search(
    _alias: String? = null,
    movieFilter: MovieFilter,
    _projection: MovieProjection.() -> MovieProjection,
  ): QueryProjection {
    field(_alias, "search", MovieProjection(), _projection, "movieFilter" to movieFilter)
    return this
  }
}
