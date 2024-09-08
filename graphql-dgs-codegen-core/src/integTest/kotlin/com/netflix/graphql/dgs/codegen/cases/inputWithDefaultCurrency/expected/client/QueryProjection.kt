package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultCurrency.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultCurrency.expected.types.OrderFilter

public class QueryProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public fun orders(filter: OrderFilter? = default<QueryProjection, OrderFilter?>("filter")):
      QueryProjection {
    field("orders", "filter" to filter)
    return this
  }
}
