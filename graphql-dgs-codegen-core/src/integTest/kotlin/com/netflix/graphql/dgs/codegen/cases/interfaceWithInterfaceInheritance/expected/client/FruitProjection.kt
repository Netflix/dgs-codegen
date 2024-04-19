package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class FruitProjection : GraphQLProjection() {
  public fun seeds(_alias: String? = null, _projection: SeedProjection.() -> SeedProjection):
      FruitProjection {
    field(_alias, "seeds", SeedProjection(), _projection)
    return this
  }

  public fun onStoneFruit(_projection: StoneFruitProjection.() -> StoneFruitProjection):
      FruitProjection {
    fragment("StoneFruit", StoneFruitProjection(), _projection)
    return this
  }
}
