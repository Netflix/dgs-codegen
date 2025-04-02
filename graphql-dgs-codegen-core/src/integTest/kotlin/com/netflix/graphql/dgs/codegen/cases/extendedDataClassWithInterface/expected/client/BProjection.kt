package com.netflix.graphql.dgs.codegen.cases.extendedDataClassWithInterface.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class BProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val age: BProjection
    get() {
      field("age")
      return this
    }

  public fun onExample(_projection: ExampleProjection.() -> ExampleProjection): BProjection {
    fragment("Example", ExampleProjection(), _projection)
    return this
  }
}
