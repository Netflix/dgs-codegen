package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class PageInfoProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val startCursor: PageInfoProjection
    get() {
      field("startCursor")
      return this
    }

  public val endCursor: PageInfoProjection
    get() {
      field("endCursor")
      return this
    }

  public val hasNextPage: PageInfoProjection
    get() {
      field("hasNextPage")
      return this
    }

  public val hasPreviousPage: PageInfoProjection
    get() {
      field("hasPreviousPage")
      return this
    }
}
