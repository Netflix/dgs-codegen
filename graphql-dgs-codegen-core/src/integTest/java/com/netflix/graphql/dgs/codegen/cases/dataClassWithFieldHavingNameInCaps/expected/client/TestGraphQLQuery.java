package com.netflix.graphql.dgs.codegen.cases.dataClassWithFieldHavingNameInCaps.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class TestGraphQLQuery extends GraphQLQuery {
  public TestGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public TestGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "test";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private String queryName;

    public TestGraphQLQuery build() {
      return new TestGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
