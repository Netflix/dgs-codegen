package com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String
import com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun `is`(_alias: String? = null, _projection: IProjection.() -> IProjection):
      QueryProjection {
    field(_alias, "is", IProjection(inputValueSerializer), _projection)
    return this
  }
}
