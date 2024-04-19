package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableListOfNullableValues.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun people(_alias: String? = null, _projection: PersonProjection.() -> PersonProjection):
      QueryProjection {
    field(_alias, "people", PersonProjection(), _projection)
    return this
  }
}
