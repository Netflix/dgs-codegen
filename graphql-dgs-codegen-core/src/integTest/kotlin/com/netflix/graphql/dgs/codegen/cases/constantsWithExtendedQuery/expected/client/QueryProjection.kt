package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedQuery.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun people(_alias: String? = null, _projection: PersonProjection.() -> PersonProjection):
      QueryProjection {
    field(_alias, "people", PersonProjection(), _projection)
    return this
  }

  public fun friends(_alias: String? = null, _projection: PersonProjection.() -> PersonProjection):
      QueryProjection {
    field(_alias, "friends", PersonProjection(), _projection)
    return this
  }
}
