package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class StoneFruitProjection : GraphQLProjection() {
  public val fuzzy: StoneFruitProjection
    get() {
      field("fuzzy")
      return this
    }

  public fun seeds(_projection: SeedProjection.() -> SeedProjection): StoneFruitProjection {
    project("seeds", SeedProjection(), _projection)
    return this
  }
}
