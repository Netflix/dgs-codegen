package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class FruitProjection : GraphQLProjection() {
  public fun seeds(_projection: SeedProjection.() -> SeedProjection): FruitProjection {
    field("seeds", SeedProjection(), _projection)
    return this
  }

  public fun onStoneFruit(_projection: StoneFruitProjection.() -> StoneFruitProjection):
      FruitProjection {
    fragment("StoneFruit", StoneFruitProjection(), _projection)
    return this
  }
}
