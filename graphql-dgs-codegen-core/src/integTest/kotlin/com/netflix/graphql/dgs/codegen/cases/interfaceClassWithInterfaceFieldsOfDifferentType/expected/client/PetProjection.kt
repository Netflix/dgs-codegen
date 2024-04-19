package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class PetProjection : GraphQLProjection() {
  public val name: PetProjection
    get() {
      field("name")
      return this
    }

  public fun diet(_alias: String? = null, _projection: DietProjection.() -> DietProjection):
      PetProjection {
    field(_alias, "diet", DietProjection(), _projection)
    return this
  }

  public fun onDog(_projection: DogProjection.() -> DogProjection): PetProjection {
    fragment("Dog", DogProjection(), _projection)
    return this
  }
}
