package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedInputTypes.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedInputTypes.expected.types.PersonFilter

public class QueryProjection : GraphQLProjection() {
  public fun people(filter: PersonFilter? = default("filter"),
      _projection: PersonProjection.() -> PersonProjection): QueryProjection {
    val args = formatArgs("filter" to filter)
    project("people($args)", PersonProjection(), _projection)
    return this
  }
}
