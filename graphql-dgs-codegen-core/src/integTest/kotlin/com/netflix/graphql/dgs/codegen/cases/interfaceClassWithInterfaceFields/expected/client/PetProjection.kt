package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFields.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class PetProjection : GraphQLProjection() {
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

  public fun mother(_alias: String? = null, _projection: PetProjection.() -> PetProjection):
      PetProjection {
    field(_alias, "mother", PetProjection(), _projection)
    return this
  }

  public fun father(_alias: String? = null, _projection: PetProjection.() -> PetProjection):
      PetProjection {
    field(_alias, "father", PetProjection(), _projection)
    return this
  }

  public fun parents(_alias: String? = null, _projection: PetProjection.() -> PetProjection):
      PetProjection {
    field(_alias, "parents", PetProjection(), _projection)
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
