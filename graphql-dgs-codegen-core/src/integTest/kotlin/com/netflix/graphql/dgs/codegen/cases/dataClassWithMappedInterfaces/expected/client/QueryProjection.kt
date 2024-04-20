package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun products(_projection: ProductProjection.() -> ProductProjection): QueryProjection {
    field("products", ProductProjection(inputValueSerializer), _projection)
    return this
  }
}
