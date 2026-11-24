package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedInterfaces.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedInterfaces.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
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

  @Generated
  public static class Builder {
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
