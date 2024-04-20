package com.netflix.graphql.dgs.codegen.cases.dataClassWithBooleanField.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class RequiredTestTypeProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val isRequired: RequiredTestTypeProjection
    get() {
      field("isRequired")
      return this
    }
}
