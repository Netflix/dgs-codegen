package com.netflix.graphql.dgs.codegen.cases.extendedDataClassWithInterface.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.extendedDataClassWithInterface.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
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
