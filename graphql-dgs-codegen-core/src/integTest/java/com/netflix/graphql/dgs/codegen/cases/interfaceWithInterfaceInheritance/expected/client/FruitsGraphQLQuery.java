package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class FruitsGraphQLQuery extends GraphQLQuery {
  public FruitsGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public FruitsGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "fruits";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private String queryName;

    public FruitsGraphQLQuery build() {
      return new FruitsGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
