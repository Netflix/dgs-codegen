package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

public class CarProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val make: CarProjection
    get() {
      field("make")
      return this
    }

  public val model: CarProjection
    get() {
      field("model")
      return this
    }

  public fun engine(_alias: String? = null, _projection: EngineProjection.() -> EngineProjection):
      CarProjection {
    field(_alias, "engine", EngineProjection(inputValueSerializer), _projection)
    return this
  }
}
