package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun fruits(_alias: String? = null, _projection: FruitProjection.() -> FruitProjection):
      QueryProjection {
    field(_alias, "fruits", FruitProjection(), _projection)
    return this
  }
}
