package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultCurrency.expected;

import java.lang.String;

public class DgsConstants {
  public static final String QUERY_TYPE = "Query";

  public static class QUERY {
    public static final String TYPE_NAME = "Query";

    public static final String Orders = "orders";

    public static class ORDERS_INPUT_ARGUMENT {
      public static final String Filter = "filter";
    }
  }

  public static class ORDERFILTER {
    public static final String TYPE_NAME = "OrderFilter";

    public static final String Value = "value";
  }
}
