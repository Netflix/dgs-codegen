package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFields.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class DogProjection : GraphQLProjection() {
  public val id: DogProjection
    get() {
      field("id")
      return this
    }

  public val name: DogProjection
    get() {
      field("name")
      return this
    }

  public val address: DogProjection
    get() {
      field("address")
      return this
    }

  public fun mother(_alias: String? = null, _projection: DogProjection.() -> DogProjection):
      DogProjection {
    field(_alias, "mother", DogProjection(), _projection)
    return this
  }

  public fun father(_alias: String? = null, _projection: DogProjection.() -> DogProjection):
      DogProjection {
    field(_alias, "father", DogProjection(), _projection)
    return this
  }

  public fun parents(_alias: String? = null, _projection: DogProjection.() -> DogProjection):
      DogProjection {
    field(_alias, "parents", DogProjection(), _projection)
    return this
  }
}
