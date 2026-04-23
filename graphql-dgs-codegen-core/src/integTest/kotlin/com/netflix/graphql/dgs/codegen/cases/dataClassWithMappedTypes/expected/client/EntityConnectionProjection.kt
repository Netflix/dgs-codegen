package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String
import com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class EntityConnectionProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun pageInfo(_alias: String? = null,
      _projection: PageInfoProjection.() -> PageInfoProjection): EntityConnectionProjection {
    field(_alias, "pageInfo", PageInfoProjection(inputValueSerializer), _projection)
    return this
  }

  public fun edges(_alias: String? = null,
      _projection: EntityEdgeProjection.() -> EntityEdgeProjection): EntityConnectionProjection {
    field(_alias, "edges", EntityEdgeProjection(inputValueSerializer), _projection)
    return this
  }
}
