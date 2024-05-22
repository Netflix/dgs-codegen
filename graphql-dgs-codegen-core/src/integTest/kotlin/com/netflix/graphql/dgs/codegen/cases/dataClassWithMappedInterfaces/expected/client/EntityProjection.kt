package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class EntityProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val id: EntityProjection
    get() {
      field("id")
      return this
    }

  public fun onProduct(_projection: ProductProjection.() -> ProductProjection): EntityProjection {
    fragment("Product", ProductProjection(), _projection)
    return this
  }
}
