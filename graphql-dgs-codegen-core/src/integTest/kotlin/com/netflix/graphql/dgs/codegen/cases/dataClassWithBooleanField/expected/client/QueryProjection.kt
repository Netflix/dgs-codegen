package com.netflix.graphql.dgs.codegen.cases.dataClassWithBooleanField.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun test(_alias: String? = null,
      _projection: RequiredTestTypeProjection.() -> RequiredTestTypeProjection): QueryProjection {
    field(_alias, "test", RequiredTestTypeProjection(), _projection)
    return this
  }
}
