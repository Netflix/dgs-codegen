package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String
import com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun products(_alias: String? = null,
      _projection: ProductProjection.() -> ProductProjection): QueryProjection {
    field(_alias, "products", ProductProjection(inputValueSerializer), _projection)
    return this
  }
}
