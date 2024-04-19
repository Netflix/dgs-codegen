package com.netflix.graphql.dgs.codegen.cases.projectionWithUnion.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun u(_projection: UProjection.() -> UProjection): QueryProjection {
    field("u", UProjection(inputValueSerializer), _projection)
    return this
  }

  public fun us(_projection: UProjection.() -> UProjection): QueryProjection {
    field("us", UProjection(inputValueSerializer), _projection)
    return this
  }
}
