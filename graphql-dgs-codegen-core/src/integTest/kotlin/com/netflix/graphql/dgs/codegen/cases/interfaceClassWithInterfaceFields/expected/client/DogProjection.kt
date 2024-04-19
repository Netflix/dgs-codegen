package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFields.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class DogProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
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
    field("mother", DogProjection(inputValueSerializer), _projection)
    return this
  }

  public fun father(_projection: DogProjection.() -> DogProjection): DogProjection {
    field("father", DogProjection(inputValueSerializer), _projection)
    return this
  }

  public fun parents(_projection: DogProjection.() -> DogProjection): DogProjection {
    field("parents", DogProjection(inputValueSerializer), _projection)
    return this
  }
}
