package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String
import com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class EngineProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
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
    field(_alias, "performance", PerformanceProjection(inputValueSerializer), _projection)
    return this
  }
}
