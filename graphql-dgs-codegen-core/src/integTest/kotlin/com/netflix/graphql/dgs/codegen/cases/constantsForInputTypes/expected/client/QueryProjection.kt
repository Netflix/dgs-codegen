package com.netflix.graphql.dgs.codegen.cases.constantsForInputTypes.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.constantsForInputTypes.expected.types.PersonFilter

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun people(filter: PersonFilter? = default<QueryProjection, PersonFilter?>("filter"),
      _projection: PersonProjection.() -> PersonProjection): QueryProjection {
    field("people", PersonProjection(inputValueSerializer), _projection, "filter" to filter)
    return this
  }
}
