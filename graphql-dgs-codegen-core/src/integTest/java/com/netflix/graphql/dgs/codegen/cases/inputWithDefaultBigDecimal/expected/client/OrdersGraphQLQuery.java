package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultBigDecimal.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultBigDecimal.expected.types.OrderFilter;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class OrdersGraphQLQuery extends GraphQLQuery {
  public OrdersGraphQLQuery(OrderFilter filter, String queryName, Set<String> fieldsSet) {
    super("query", queryName);
    if (filter != null || fieldsSet.contains("filter")) {
        getInput().put("filter", filter);
    }
  }

  public OrdersGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "orders";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private OrderFilter filter;

    private String queryName;

    public OrdersGraphQLQuery build() {
      return new OrdersGraphQLQuery(filter, queryName, fieldsSet);
               
    }

    public Builder filter(OrderFilter filter) {
      this.filter = filter;
      this.fieldsSet.add("filter");
      return this;
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
