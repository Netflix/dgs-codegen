package com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsWithExtendedQuery.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsWithExtendedQuery.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
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

  @Generated
  public static class Builder {
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
