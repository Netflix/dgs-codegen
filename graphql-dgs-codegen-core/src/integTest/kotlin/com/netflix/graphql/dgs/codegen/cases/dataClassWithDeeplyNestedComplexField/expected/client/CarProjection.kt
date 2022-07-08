package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class CarProjection : GraphQLProjection() {
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

  public fun engine(_projection: EngineProjection.() -> EngineProjection): CarProjection {
    project("engine", EngineProjection(), _projection)
    return this
  }
}
