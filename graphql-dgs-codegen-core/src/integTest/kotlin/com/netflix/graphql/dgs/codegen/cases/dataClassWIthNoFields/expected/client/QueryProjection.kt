package com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun me(_alias: String? = null, _projection: PersonProjection.() -> PersonProjection):
      QueryProjection {
    field(_alias, "me", PersonProjection(), _projection)
    return this
  }
}
