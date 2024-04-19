package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun cars(_alias: String? = null, _projection: CarProjection.() -> CarProjection):
      QueryProjection {
    field(_alias, "cars", CarProjection(), _projection)
    return this
  }
}
