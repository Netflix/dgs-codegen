package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class SearchResultPageProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun items(_projection: SearchResultProjection.() -> SearchResultProjection):
      SearchResultPageProjection {
    field("items", SearchResultProjection(inputValueSerializer), _projection)
    return this
  }
}
