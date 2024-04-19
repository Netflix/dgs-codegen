package com.netflix.graphql.dgs.codegen.cases.projectionWithUnion.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun u(_alias: String? = null, _projection: UProjection.() -> UProjection):
      QueryProjection {
    field(_alias, "u", UProjection(), _projection)
    return this
  }

  public fun us(_alias: String? = null, _projection: UProjection.() -> UProjection):
      QueryProjection {
    field(_alias, "us", UProjection(), _projection)
    return this
  }
}
