package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class StoneFruitProjection : GraphQLProjection() {
  public val fuzzy: StoneFruitProjection
    get() {
      field("fuzzy")
      return this
    }

  public fun seeds(_alias: String? = null, _projection: SeedProjection.() -> SeedProjection):
      StoneFruitProjection {
    field(_alias, "seeds", SeedProjection(), _projection)
    return this
  }
}
