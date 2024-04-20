package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableAndInterface.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun people(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    field("people", PersonProjection(inputValueSerializer), _projection)
    return this
  }
}
