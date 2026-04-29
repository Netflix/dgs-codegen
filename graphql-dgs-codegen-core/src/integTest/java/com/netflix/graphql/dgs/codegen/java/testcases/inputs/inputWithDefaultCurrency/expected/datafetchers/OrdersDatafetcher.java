package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultCurrency.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import graphql.schema.DataFetchingEnvironment;
import java.lang.String;

@DgsComponent
public class OrdersDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "orders"
  )
  public String getOrders(DataFetchingEnvironment dataFetchingEnvironment) {
    return "";
  }
}
