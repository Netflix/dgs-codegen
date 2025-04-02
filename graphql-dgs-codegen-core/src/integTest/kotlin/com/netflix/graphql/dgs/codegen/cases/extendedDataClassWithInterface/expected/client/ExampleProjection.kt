package com.netflix.graphql.dgs.codegen.cases.extendedDataClassWithInterface.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class ExampleProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val name: ExampleProjection
    get() {
      field("name")
      return this
    }

  public val age: ExampleProjection
    get() {
      field("age")
      return this
    }
}
