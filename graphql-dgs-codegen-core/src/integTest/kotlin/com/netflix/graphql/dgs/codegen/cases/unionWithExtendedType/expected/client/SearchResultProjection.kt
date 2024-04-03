package com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class SearchResultProjection : GraphQLProjection() {
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
