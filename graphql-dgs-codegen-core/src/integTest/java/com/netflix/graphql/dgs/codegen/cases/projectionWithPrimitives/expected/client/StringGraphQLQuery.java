package com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitives.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class StringGraphQLQuery extends GraphQLQuery {
  public StringGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public StringGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "string";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private String queryName;

    public StringGraphQLQuery build() {
      return new StringGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
