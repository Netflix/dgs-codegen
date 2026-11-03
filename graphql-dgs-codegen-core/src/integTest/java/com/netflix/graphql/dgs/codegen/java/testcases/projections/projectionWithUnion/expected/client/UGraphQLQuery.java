package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithUnion.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithUnion.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
public class UGraphQLQuery extends GraphQLQuery {
  public UGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public UGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "u";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  @Generated
  public static class Builder {
    private String queryName;

    public UGraphQLQuery build() {
      return new UGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
