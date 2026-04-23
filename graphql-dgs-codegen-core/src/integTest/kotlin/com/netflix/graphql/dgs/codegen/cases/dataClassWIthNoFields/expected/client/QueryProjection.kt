package com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String
import com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun me(_alias: String? = null, _projection: PersonProjection.() -> PersonProjection):
      QueryProjection {
    field(_alias, "me", PersonProjection(inputValueSerializer), _projection)
    return this
  }
}
