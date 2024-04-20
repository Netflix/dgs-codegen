package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun fruits(_alias: String? = null, _projection: FruitProjection.() -> FruitProjection):
      QueryProjection {
    field(_alias, "fruits", FruitProjection(inputValueSerializer), _projection)
    return this
  }
}
