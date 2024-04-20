package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun search(
    _alias: String? = null,
    text: String,
    _projection: SearchResultPageProjection.() -> SearchResultPageProjection,
  ): QueryProjection {
    field(_alias, "search", SearchResultPageProjection(inputValueSerializer), _projection, "text" to
        text)
    return this
  }
}
