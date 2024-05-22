package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class DietProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val calories: DietProjection
    get() {
      field("calories")
      return this
    }

  public fun onVegetarian(_projection: VegetarianProjection.() -> VegetarianProjection):
      DietProjection {
    fragment("Vegetarian", VegetarianProjection(), _projection)
    return this
  }
}
