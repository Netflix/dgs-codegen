package com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected.types.I
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun person(
    _alias: String? = null,
    a1: String? = default<QueryProjection, String?>("a1"),
    a2: String,
    a3: I? = default<QueryProjection, I?>("a3"),
    _projection: PersonProjection.() -> PersonProjection,
  ): QueryProjection {
    field(_alias, "person", PersonProjection(), _projection, "a1" to a1 , "a2" to a2 , "a3" to a3)
    return this
  }
}
