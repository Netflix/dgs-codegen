package com.netflix.graphql.dgs.codegen.java.testcases.unions.union.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.unions.union.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
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

  @Generated
  public static class Builder {
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
