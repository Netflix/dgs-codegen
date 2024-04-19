package com.netflix.graphql.dgs.codegen.cases.projectionWithUnion.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class UProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun onEmployee(_projection: EmployeeProjection.() -> EmployeeProjection): UProjection {
    fragment("Employee", EmployeeProjection(), _projection)
    return this
  }
}
