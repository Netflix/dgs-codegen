package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.Generated
import kotlin.String

@Generated
public class DogProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val name: DogProjection
    get() {
      field("name")
      return this
    }

  public fun diet(_alias: String? = null,
      _projection: VegetarianProjection.() -> VegetarianProjection): DogProjection {
    field(_alias, "diet", VegetarianProjection(inputValueSerializer), _projection)
    return this
  }
}
