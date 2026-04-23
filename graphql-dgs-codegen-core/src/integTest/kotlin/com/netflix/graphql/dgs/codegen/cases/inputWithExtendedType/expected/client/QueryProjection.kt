package com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.types.MovieFilter
import com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun movies(filter: MovieFilter? = default<QueryProjection, MovieFilter?>("filter")):
      QueryProjection {
    field("movies", "filter" to filter)
    return this
  }
}
