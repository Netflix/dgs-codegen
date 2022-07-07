package kotlin2.union.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun search(_projection: SearchResultProjection.() -> SearchResultProjection):
      QueryProjection {
    project("search", SearchResultProjection(), _projection)
    return this
  }
}
