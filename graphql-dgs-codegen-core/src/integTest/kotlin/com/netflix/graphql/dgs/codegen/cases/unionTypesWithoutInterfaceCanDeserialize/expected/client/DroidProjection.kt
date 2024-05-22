package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class DroidProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val id: DroidProjection
    get() {
      field("id")
      return this
    }

  public val name: DroidProjection
    get() {
      field("name")
      return this
    }

  public val primaryFunction: DroidProjection
    get() {
      field("primaryFunction")
      return this
    }
}
