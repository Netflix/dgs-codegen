package kotlin2.interfaceClassWithInterfaceFields.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

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

  public fun mother(_projection: DogProjection.() -> DogProjection): DogProjection {
    project("mother", DogProjection(), _projection)
    return this
  }

  public fun father(_projection: DogProjection.() -> DogProjection): DogProjection {
    project("father", DogProjection(), _projection)
    return this
  }

  public fun parents(_projection: DogProjection.() -> DogProjection): DogProjection {
    project("parents", DogProjection(), _projection)
    return this
  }
}
