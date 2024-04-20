package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFields.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class PetProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val id: PetProjection
    get() {
      field("id")
      return this
    }

  public val name: PetProjection
    get() {
      field("name")
      return this
    }

  public val address: PetProjection
    get() {
      field("address")
      return this
    }

  public fun mother(_projection: PetProjection.() -> PetProjection): PetProjection {
    field("mother", PetProjection(inputValueSerializer), _projection)
    return this
  }

  public fun father(_projection: PetProjection.() -> PetProjection): PetProjection {
    field("father", PetProjection(inputValueSerializer), _projection)
    return this
  }

  public fun parents(_projection: PetProjection.() -> PetProjection): PetProjection {
    field("parents", PetProjection(inputValueSerializer), _projection)
    return this
  }

  public fun onDog(_projection: DogProjection.() -> DogProjection): PetProjection {
    fragment("Dog", DogProjection(), _projection)
    return this
  }

  public fun onBird(_projection: BirdProjection.() -> BirdProjection): PetProjection {
    fragment("Bird", BirdProjection(), _projection)
    return this
  }
}
