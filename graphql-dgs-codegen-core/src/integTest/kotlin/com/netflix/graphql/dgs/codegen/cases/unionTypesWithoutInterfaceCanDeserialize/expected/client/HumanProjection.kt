package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class HumanProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val id: HumanProjection
    get() {
      field("id")
      return this
    }

  public val name: HumanProjection
    get() {
      field("name")
      return this
    }

  public val totalCredits: HumanProjection
    get() {
      field("totalCredits")
      return this
    }
}
