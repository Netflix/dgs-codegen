package com.netflix.graphql.dgs.codegen.cases.projectionWithUnion.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String
import com.netflix.graphql.dgs.codegen.cases.projectionWithUnion.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun u(_alias: String? = null, _projection: UProjection.() -> UProjection):
      QueryProjection {
    field(_alias, "u", UProjection(inputValueSerializer), _projection)
    return this
  }

  public fun us(_alias: String? = null, _projection: UProjection.() -> UProjection):
      QueryProjection {
    field(_alias, "us", UProjection(inputValueSerializer), _projection)
    return this
  }
}
