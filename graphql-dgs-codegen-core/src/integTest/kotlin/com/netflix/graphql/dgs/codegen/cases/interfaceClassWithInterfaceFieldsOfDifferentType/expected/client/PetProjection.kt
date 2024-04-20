package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class PetProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val name: PetProjection
    get() {
      field("name")
      return this
    }

  public fun diet(_projection: DietProjection.() -> DietProjection): PetProjection {
    field("diet", DietProjection(inputValueSerializer), _projection)
    return this
  }

  public fun onDog(_projection: DogProjection.() -> DogProjection): PetProjection {
    fragment("Dog", DogProjection(), _projection)
    return this
  }
}
