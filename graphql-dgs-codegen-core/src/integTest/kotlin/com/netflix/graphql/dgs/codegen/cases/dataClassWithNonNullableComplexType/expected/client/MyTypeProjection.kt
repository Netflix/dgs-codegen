package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableComplexType.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class MyTypeProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun other(_alias: String? = null,
      _projection: OtherTypeProjection.() -> OtherTypeProjection): MyTypeProjection {
    field(_alias, "other", OtherTypeProjection(inputValueSerializer), _projection)
    return this
  }
}
