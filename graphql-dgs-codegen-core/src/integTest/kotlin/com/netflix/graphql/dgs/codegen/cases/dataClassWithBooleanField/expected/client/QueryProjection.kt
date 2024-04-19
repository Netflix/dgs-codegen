package com.netflix.graphql.dgs.codegen.cases.dataClassWithBooleanField.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun test(_projection: RequiredTestTypeProjection.() -> RequiredTestTypeProjection):
      QueryProjection {
    field("test", RequiredTestTypeProjection(inputValueSerializer), _projection)
    return this
  }
}
