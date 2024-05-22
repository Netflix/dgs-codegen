package com.netflix.graphql.dgs.codegen.cases.projectionWithEnum.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val e: QueryProjection
    get() {
      field("e")
      return this
    }

  public val es: QueryProjection
    get() {
      field("es")
      return this
    }
}
