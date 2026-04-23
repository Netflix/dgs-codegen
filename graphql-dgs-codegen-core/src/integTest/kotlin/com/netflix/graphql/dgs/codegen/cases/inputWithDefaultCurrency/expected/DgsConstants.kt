package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultCurrency.expected

import kotlin.String

@jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Orders: String = "orders"

    @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
    @Generated
    public object ORDERS_INPUT_ARGUMENT {
      public const val Filter: String = "filter"
    }
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object ORDERFILTER {
    public const val TYPE_NAME: String = "OrderFilter"

    public const val Value: String = "value"
  }
}
