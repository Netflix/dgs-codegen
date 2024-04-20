package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFields.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class BirdProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
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
    field("mother", BirdProjection(inputValueSerializer), _projection)
    return this
  }

  public fun father(_projection: BirdProjection.() -> BirdProjection): BirdProjection {
    field("father", BirdProjection(inputValueSerializer), _projection)
    return this
  }

  public fun parents(_projection: BirdProjection.() -> BirdProjection): BirdProjection {
    field("parents", BirdProjection(inputValueSerializer), _projection)
    return this
  }
}
