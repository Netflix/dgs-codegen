package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.Generated

@Generated
public class PerformanceProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val zeroToSixty: PerformanceProjection
    get() {
      field("zeroToSixty")
      return this
    }

  public val quarterMile: PerformanceProjection
    get() {
      field("quarterMile")
      return this
    }
}
