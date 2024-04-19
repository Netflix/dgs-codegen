package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedInputTypes.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedInputTypes.expected.types.PersonFilter
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun people(
    _alias: String? = null,
    filter: PersonFilter? = default<QueryProjection, PersonFilter?>("filter"),
    _projection: PersonProjection.() -> PersonProjection,
  ): QueryProjection {
    field(_alias, "people", PersonProjection(), _projection, "filter" to filter)
    return this
  }
}
