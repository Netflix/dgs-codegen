package com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected.types.I
import kotlin.String

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun person(
    a1: String? = default<QueryProjection, String?>("a1"),
    a2: String,
    a3: I? = default<QueryProjection, I?>("a3"),
    _alias: String? = null,
    _projection: PersonProjection.() -> PersonProjection,
  ): QueryProjection {
    field(_alias, "person", PersonProjection(inputValueSerializer), _projection, "a1" to a1, "a2" to
        a2, "a3" to a3)
    return this
  }
}
