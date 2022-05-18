package kotlin2.unionWithExtendedType.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun search(_projection: SearchResultProjection.() -> SearchResultProjection):
      QueryProjection {
    project("search", SearchResultProjection(), _projection)
    return this
  }
}
