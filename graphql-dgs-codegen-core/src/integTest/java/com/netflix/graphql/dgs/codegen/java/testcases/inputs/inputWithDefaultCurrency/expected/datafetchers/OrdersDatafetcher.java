package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultCurrency.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultCurrency.expected.Generated;
import graphql.schema.DataFetchingEnvironment;
import java.lang.String;

@Generated
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
