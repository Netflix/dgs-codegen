package com.netflix.graphql.dgs.codegen.cases.`enum`.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.`enum`.expected.Generated

@Generated
public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val types: QueryProjection
    get() {
      field("types")
      return this
    }
}
