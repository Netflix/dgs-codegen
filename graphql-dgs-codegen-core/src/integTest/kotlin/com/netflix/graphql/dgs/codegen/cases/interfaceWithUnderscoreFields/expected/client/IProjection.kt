package com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class IProjection : GraphQLProjection() {
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
