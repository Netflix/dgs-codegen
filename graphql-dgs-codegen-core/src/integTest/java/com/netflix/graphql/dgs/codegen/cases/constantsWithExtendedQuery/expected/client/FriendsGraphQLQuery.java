package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedQuery.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class FriendsGraphQLQuery extends GraphQLQuery {
  public FriendsGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public FriendsGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "friends";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private String queryName;

    public FriendsGraphQLQuery build() {
      return new FriendsGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
