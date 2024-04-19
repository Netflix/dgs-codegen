package com.netflix.graphql.dgs.codegen.cases.dataClassWithRecursiveField.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class PersonProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
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

  public fun friends(_projection: PersonProjection.() -> PersonProjection): PersonProjection {
    field("friends", PersonProjection(inputValueSerializer), _projection)
    return this
  }
}
