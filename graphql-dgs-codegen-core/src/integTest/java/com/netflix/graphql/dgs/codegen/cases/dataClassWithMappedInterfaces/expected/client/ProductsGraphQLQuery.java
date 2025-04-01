package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class ProductsGraphQLQuery extends GraphQLQuery {
  public ProductsGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public ProductsGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "products";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private String queryName;

    public ProductsGraphQLQuery build() {
      return new ProductsGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
