package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFields.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class BirdProjection : GraphQLProjection() {
  public val id: BirdProjection
    get() {
      field("id")
      return this
    }

  public val name: BirdProjection
    get() {
      field("name")
      return this
    }

  public val address: BirdProjection
    get() {
      field("address")
      return this
    }

  public fun mother(_alias: String? = null, _projection: BirdProjection.() -> BirdProjection):
      BirdProjection {
    field(_alias, "mother", BirdProjection(), _projection)
    return this
  }

  public fun father(_alias: String? = null, _projection: BirdProjection.() -> BirdProjection):
      BirdProjection {
    field(_alias, "father", BirdProjection(), _projection)
    return this
  }

  public fun parents(_alias: String? = null, _projection: BirdProjection.() -> BirdProjection):
      BirdProjection {
    field(_alias, "parents", BirdProjection(), _projection)
    return this
  }
}
