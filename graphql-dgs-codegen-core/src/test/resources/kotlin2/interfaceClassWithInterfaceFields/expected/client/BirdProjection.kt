package kotlin2.interfaceClassWithInterfaceFields.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

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

  public fun mother(_projection: BirdProjection.() -> BirdProjection): BirdProjection {
    project("mother", BirdProjection(), _projection)
    return this
  }

  public fun father(_projection: BirdProjection.() -> BirdProjection): BirdProjection {
    project("father", BirdProjection(), _projection)
    return this
  }

  public fun parents(_projection: BirdProjection.() -> BirdProjection): BirdProjection {
    project("parents", BirdProjection(), _projection)
    return this
  }
}
