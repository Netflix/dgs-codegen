package com.netflix.graphql.dgs.codegen.cases.union.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class SearchGraphQLQuery extends GraphQLQuery {
  public SearchGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public SearchGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "search";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private String queryName;

    public SearchGraphQLQuery build() {
      return new SearchGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
