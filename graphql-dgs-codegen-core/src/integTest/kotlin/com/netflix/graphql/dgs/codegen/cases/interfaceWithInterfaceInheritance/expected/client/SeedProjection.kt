package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class SeedProjection : GraphQLProjection() {
  public val name: SeedProjection
    get() {
      field("name")
      return this
    }
}
