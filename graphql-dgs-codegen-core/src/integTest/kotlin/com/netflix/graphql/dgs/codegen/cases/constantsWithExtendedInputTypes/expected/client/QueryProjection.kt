package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedInputTypes.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedInputTypes.expected.types.PersonFilter
import kotlin.String

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun people(
    filter: PersonFilter? = default<QueryProjection, PersonFilter?>("filter"),
    _alias: String? = null,
    _projection: PersonProjection.() -> PersonProjection,
  ): QueryProjection {
    field(_alias, "people", PersonProjection(inputValueSerializer), _projection, "filter" to filter)
    return this
  }
}
