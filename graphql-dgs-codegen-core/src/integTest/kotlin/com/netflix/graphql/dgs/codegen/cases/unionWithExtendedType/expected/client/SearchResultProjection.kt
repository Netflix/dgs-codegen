package com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class SearchResultProjection : GraphQLProjection() {
  public fun onMovie(_projection: MovieProjection.() -> MovieProjection): SearchResultProjection {
    project("... on Movie", MovieProjection(), _projection)
    return this
  }

  public fun onActor(_projection: ActorProjection.() -> ActorProjection): SearchResultProjection {
    project("... on Actor", ActorProjection(), _projection)
    return this
  }

  public fun onRating(_projection: RatingProjection.() -> RatingProjection):
      SearchResultProjection {
    project("... on Rating", RatingProjection(), _projection)
    return this
  }
}
