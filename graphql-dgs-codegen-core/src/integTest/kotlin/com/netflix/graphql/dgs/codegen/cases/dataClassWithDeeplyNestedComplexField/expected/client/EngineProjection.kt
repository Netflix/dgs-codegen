package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class EngineProjection : GraphQLProjection() {
  public val type: EngineProjection
    get() {
      field("type")
      return this
    }

  public val bhp: EngineProjection
    get() {
      field("bhp")
      return this
    }

  public val size: EngineProjection
    get() {
      field("size")
      return this
    }

  public fun performance(_projection: PerformanceProjection.() -> PerformanceProjection):
      EngineProjection {
    project("performance", PerformanceProjection(), _projection)
    return this
  }
}
