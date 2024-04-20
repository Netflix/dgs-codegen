package com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.types.I1
import com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.types.I2
import kotlin.String

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun q1(arg1: String? = default<QueryProjection, String?>("arg1"), arg2: I2? =
      default<QueryProjection, I2?>("arg2")): QueryProjection {
    field("q1", "arg1" to arg1 , "arg2" to arg2)
    return this
  }

  public fun q2(arg1: I1? = default<QueryProjection, I1?>("arg1"), arg2: String? =
      default<QueryProjection, String?>("arg2")): QueryProjection {
    field("q2", "arg1" to arg1 , "arg2" to arg2)
    return this
  }
}
