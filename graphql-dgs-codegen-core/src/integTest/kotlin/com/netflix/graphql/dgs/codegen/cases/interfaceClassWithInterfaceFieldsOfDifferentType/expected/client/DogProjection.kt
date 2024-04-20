package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class DogProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val name: DogProjection
    get() {
      field("name")
      return this
    }

  public fun diet(_projection: VegetarianProjection.() -> VegetarianProjection): DogProjection {
    field("diet", VegetarianProjection(inputValueSerializer), _projection)
    return this
  }
}
