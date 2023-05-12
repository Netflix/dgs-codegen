package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

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
