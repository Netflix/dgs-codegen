package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun products(_alias: String? = null,
      _projection: ProductProjection.() -> ProductProjection): QueryProjection {
    field(_alias, "products", ProductProjection(), _projection)
    return this
  }
}
