package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class DogProjection : GraphQLProjection() {
  public val name: DogProjection
    get() {
      field("name")
      return this
    }

  public fun diet(_alias: String? = null,
      _projection: VegetarianProjection.() -> VegetarianProjection): DogProjection {
    field(_alias, "diet", VegetarianProjection(), _projection)
    return this
  }
}
