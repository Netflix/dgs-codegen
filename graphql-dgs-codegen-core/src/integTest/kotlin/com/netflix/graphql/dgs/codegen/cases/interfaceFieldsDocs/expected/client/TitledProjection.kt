package com.netflix.graphql.dgs.codegen.cases.interfaceFieldsDocs.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class TitledProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val title: TitledProjection
    get() {
      field("title")
      return this
    }
}
