package com.netflix.graphql.dgs.codegen.cases.extendedDataClassWithInterface.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.extendedDataClassWithInterface.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
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
