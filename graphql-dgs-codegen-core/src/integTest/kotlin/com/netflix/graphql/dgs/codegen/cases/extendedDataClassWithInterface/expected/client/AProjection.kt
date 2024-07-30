package com.netflix.graphql.dgs.codegen.cases.extendedDataClassWithInterface.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class AProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val name: AProjection
    get() {
      field("name")
      return this
    }

  public fun onExample(_projection: ExampleProjection.() -> ExampleProjection): AProjection {
    fragment("Example", ExampleProjection(), _projection)
    return this
  }
}
