package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedQuery.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun people(_alias: String? = null, _projection: PersonProjection.() -> PersonProjection):
      QueryProjection {
    field(_alias, "people", PersonProjection(inputValueSerializer), _projection)
    return this
  }

  public fun friends(_alias: String? = null, _projection: PersonProjection.() -> PersonProjection):
      QueryProjection {
    field(_alias, "friends", PersonProjection(inputValueSerializer), _projection)
    return this
  }
}
