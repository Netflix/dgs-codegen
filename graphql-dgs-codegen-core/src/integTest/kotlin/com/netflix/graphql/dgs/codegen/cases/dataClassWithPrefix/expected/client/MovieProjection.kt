package com.netflix.graphql.dgs.codegen.cases.dataClassWithPrefix.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class MovieProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val title: MovieProjection
    get() {
      field("title")
      return this
    }
}
