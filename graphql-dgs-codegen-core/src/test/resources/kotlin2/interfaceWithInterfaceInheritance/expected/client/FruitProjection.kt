package kotlin2.interfaceWithInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class FruitProjection : GraphQLProjection() {
  public fun seeds(_projection: SeedProjection.() -> SeedProjection): FruitProjection {
    project("seeds", SeedProjection(), _projection)
    return this
  }

  public fun onStoneFruit(_projection: StoneFruitProjection.() -> StoneFruitProjection):
      FruitProjection {
    project("... on StoneFruit", StoneFruitProjection(), _projection)
    return this
  }
}
