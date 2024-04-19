package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class SearchResultPageProjection : GraphQLProjection() {
  public fun items(_alias: String? = null,
      _projection: SearchResultProjection.() -> SearchResultProjection):
      SearchResultPageProjection {
    field(_alias, "items", SearchResultProjection(), _projection)
    return this
  }
}
