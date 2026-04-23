package com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class SearchResultProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun onMovie(_projection: MovieProjection.() -> MovieProjection): SearchResultProjection {
    fragment("Movie", MovieProjection(), _projection)
    return this
  }

  public fun onActor(_projection: ActorProjection.() -> ActorProjection): SearchResultProjection {
    fragment("Actor", ActorProjection(), _projection)
    return this
  }

  public fun onRating(_projection: RatingProjection.() -> RatingProjection):
      SearchResultProjection {
    fragment("Rating", RatingProjection(), _projection)
    return this
  }
}
