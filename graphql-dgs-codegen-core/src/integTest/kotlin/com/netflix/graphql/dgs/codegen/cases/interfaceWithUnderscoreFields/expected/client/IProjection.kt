package com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class IProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val _id: IProjection
    get() {
      field("_id")
      return this
    }

  public fun onT(_projection: TProjection.() -> TProjection): IProjection {
    fragment("T", TProjection(), _projection)
    return this
  }
}
