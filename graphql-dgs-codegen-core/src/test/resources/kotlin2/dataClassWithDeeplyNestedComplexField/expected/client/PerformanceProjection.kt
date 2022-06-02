package kotlin2.dataClassWithDeeplyNestedComplexField.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class PerformanceProjection : GraphQLProjection() {
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
