package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithUnion.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithUnion.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
public class UsGraphQLQuery extends GraphQLQuery {
  public UsGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public UsGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "us";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  @Generated
  public static class Builder {
    private String queryName;

    public UsGraphQLQuery build() {
      return new UsGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
