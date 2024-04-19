package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class VegetarianProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val calories: VegetarianProjection
    get() {
      field("calories")
      return this
    }

  public val vegetables: VegetarianProjection
    get() {
      field("vegetables")
      return this
    }
}
