package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun cars(_projection: CarProjection.() -> CarProjection): QueryProjection {
    field("cars", CarProjection(inputValueSerializer), _projection)
    return this
  }
}
