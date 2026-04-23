package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultBigDecimal.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultBigDecimal.expected.types.OrderFilter
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultBigDecimal.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun orders(filter: OrderFilter? = default<QueryProjection, OrderFilter?>("filter")):
      QueryProjection {
    field("orders", "filter" to filter)
    return this
  }
}
