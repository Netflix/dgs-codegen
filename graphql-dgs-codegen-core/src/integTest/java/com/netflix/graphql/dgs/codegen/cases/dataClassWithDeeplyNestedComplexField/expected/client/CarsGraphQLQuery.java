package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

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

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

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
