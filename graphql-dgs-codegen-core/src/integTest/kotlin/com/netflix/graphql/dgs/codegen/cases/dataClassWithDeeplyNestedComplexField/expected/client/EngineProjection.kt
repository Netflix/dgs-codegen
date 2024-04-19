package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String

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

  public fun performance(_alias: String? = null,
      _projection: PerformanceProjection.() -> PerformanceProjection): EngineProjection {
    field(_alias, "performance", PerformanceProjection(), _projection)
    return this
  }
}
