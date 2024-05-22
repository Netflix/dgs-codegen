package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class NodeProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val id: NodeProjection
    get() {
      field("id")
      return this
    }

  public fun onEntity(_projection: EntityProjection.() -> EntityProjection): NodeProjection {
    fragment("Entity", EntityProjection(), _projection)
    return this
  }

  public fun onProduct(_projection: ProductProjection.() -> ProductProjection): NodeProjection {
    fragment("Product", ProductProjection(), _projection)
    return this
  }
}
