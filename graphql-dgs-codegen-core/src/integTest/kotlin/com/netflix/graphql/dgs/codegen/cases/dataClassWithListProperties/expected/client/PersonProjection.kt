package com.netflix.graphql.dgs.codegen.cases.dataClassWithListProperties.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class PersonProjection : GraphQLProjection() {
  public val name: PersonProjection
    get() {
      field("name")
      return this
    }

  public val email: PersonProjection
    get() {
      field("email")
      return this
    }
}
