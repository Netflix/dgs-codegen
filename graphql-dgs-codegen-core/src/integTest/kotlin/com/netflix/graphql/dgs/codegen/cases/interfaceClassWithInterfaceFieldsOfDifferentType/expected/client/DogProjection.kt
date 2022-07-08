package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class DogProjection : GraphQLProjection() {
  public val name: DogProjection
    get() {
      field("name")
      return this
    }

  public fun diet(_projection: VegetarianProjection.() -> VegetarianProjection): DogProjection {
    project("diet", VegetarianProjection(), _projection)
    return this
  }
}
