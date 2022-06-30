package kotlin2.unionTypesWithoutInterfaceCanDeserialize.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class SearchResultPageProjection : GraphQLProjection() {
  public fun items(_projection: SearchResultProjection.() -> SearchResultProjection):
      SearchResultPageProjection {
    project("items", SearchResultProjection(), _projection)
    return this
  }
}
