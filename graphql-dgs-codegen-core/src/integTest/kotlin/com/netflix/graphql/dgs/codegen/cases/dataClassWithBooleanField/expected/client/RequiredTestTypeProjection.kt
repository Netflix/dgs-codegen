package com.netflix.graphql.dgs.codegen.cases.dataClassWithBooleanField.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class RequiredTestTypeProjection : GraphQLProjection() {
  public val isRequired: RequiredTestTypeProjection
    get() {
      field("isRequired")
      return this
    }
}
