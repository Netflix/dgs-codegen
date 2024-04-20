package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun fruits(_projection: FruitProjection.() -> FruitProjection): QueryProjection {
    field("fruits", FruitProjection(inputValueSerializer), _projection)
    return this
  }
}
