package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableComplexType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class MyTypeProjection : GraphQLProjection() {
  public fun other(_alias: String? = null,
      _projection: OtherTypeProjection.() -> OtherTypeProjection): MyTypeProjection {
    field(_alias, "other", OtherTypeProjection(), _projection)
    return this
  }
}
