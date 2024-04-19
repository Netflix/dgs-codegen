package com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class SampleTypeProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val `return`: SampleTypeProjection
    get() {
      field("return")
      return this
    }
}
