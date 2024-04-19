package com.netflix.graphql.dgs.codegen.cases.dataClassWithRecursiveField.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class PersonProjection : GraphQLProjection() {
  public val firstname: PersonProjection
    get() {
      field("firstname")
      return this
    }

  public val lastname: PersonProjection
    get() {
      field("lastname")
      return this
    }

  public fun friends(_alias: String? = null, _projection: PersonProjection.() -> PersonProjection):
      PersonProjection {
    field(_alias, "friends", PersonProjection(), _projection)
    return this
  }
}
