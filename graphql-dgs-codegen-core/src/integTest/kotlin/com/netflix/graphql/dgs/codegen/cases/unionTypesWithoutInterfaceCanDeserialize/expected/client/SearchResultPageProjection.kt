package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class SearchResultPageProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun items(_alias: String? = null,
      _projection: SearchResultProjection.() -> SearchResultProjection):
      SearchResultPageProjection {
    field(_alias, "items", SearchResultProjection(inputValueSerializer), _projection)
    return this
  }
}
