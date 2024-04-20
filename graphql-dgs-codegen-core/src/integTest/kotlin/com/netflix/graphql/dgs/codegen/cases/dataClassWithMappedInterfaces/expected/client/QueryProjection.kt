package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun products(_alias: String? = null,
      _projection: ProductProjection.() -> ProductProjection): QueryProjection {
    field(_alias, "products", ProductProjection(inputValueSerializer), _projection)
    return this
  }
}
