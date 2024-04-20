package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun cars(_alias: String? = null, _projection: CarProjection.() -> CarProjection):
      QueryProjection {
    field(_alias, "cars", CarProjection(inputValueSerializer), _projection)
    return this
  }
}
