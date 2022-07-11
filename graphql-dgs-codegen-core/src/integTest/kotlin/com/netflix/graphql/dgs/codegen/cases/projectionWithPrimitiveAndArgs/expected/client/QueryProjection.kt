package com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitiveAndArgs.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitiveAndArgs.expected.types.I
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun string(
    a1: String? = default("a1"),
    a2: String,
    a3: I? = default("a3"),
  ): QueryProjection {
    val args = formatArgs("a1" to a1, "a2" to a2, "a3" to a3)
    field("string($args)")
    return this
  }
}
