package com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class TProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val _id: TProjection
    get() {
      field("_id")
      return this
    }

  public val id: TProjection
    get() {
      field("id")
      return this
    }
}
