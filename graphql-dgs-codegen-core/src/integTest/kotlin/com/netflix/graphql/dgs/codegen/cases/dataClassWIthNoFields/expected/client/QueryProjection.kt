package com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun me(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    field("me", PersonProjection(inputValueSerializer), _projection)
    return this
  }
}
