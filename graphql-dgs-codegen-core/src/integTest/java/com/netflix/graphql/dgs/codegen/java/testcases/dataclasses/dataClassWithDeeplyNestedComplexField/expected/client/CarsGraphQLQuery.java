package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeeplyNestedComplexField.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeeplyNestedComplexField.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
public class CarsGraphQLQuery extends GraphQLQuery {
  public CarsGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public CarsGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "cars";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  @Generated
  public static class Builder {
    private String queryName;

    public CarsGraphQLQuery build() {
      return new CarsGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
