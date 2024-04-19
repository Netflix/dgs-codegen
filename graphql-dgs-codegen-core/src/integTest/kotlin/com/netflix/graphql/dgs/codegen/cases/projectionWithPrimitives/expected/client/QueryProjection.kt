package com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitives.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val string: QueryProjection
    get() {
      field("string")
      return this
    }

  public val strings: QueryProjection
    get() {
      field("strings")
      return this
    }
}
