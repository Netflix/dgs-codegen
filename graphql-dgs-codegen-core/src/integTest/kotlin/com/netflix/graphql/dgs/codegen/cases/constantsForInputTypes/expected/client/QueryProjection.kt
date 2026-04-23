package com.netflix.graphql.dgs.codegen.cases.constantsForInputTypes.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.constantsForInputTypes.expected.types.PersonFilter
import kotlin.String
import com.netflix.graphql.dgs.codegen.cases.constantsForInputTypes.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun people(
    filter: PersonFilter? = default<QueryProjection, PersonFilter?>("filter"),
    _alias: String? = null,
    _projection: PersonProjection.() -> PersonProjection,
  ): QueryProjection {
    field(_alias, "people", PersonProjection(inputValueSerializer), _projection, "filter" to filter)
    return this
  }
}
