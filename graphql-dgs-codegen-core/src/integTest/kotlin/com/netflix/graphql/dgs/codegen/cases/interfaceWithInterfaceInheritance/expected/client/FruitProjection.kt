package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class FruitProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun seeds(_projection: SeedProjection.() -> SeedProjection): FruitProjection {
    field("seeds", SeedProjection(inputValueSerializer), _projection)
    return this
  }

  public fun onStoneFruit(_projection: StoneFruitProjection.() -> StoneFruitProjection):
      FruitProjection {
    fragment("StoneFruit", StoneFruitProjection(), _projection)
    return this
  }
}
