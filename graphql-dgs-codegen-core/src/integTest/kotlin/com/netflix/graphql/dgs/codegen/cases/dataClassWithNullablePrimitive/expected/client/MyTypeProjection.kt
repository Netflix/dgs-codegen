package com.netflix.graphql.dgs.codegen.cases.dataClassWithNullablePrimitive.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.dataClassWithNullablePrimitive.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class MyTypeProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val count: MyTypeProjection
    get() {
      field("count")
      return this
    }

  public val truth: MyTypeProjection
    get() {
      field("truth")
      return this
    }

  public val floaty: MyTypeProjection
    get() {
      field("floaty")
      return this
    }
}
