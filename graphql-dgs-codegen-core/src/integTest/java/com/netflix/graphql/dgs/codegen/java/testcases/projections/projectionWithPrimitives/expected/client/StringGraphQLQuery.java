package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithPrimitives.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithPrimitives.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
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

  @Generated
  public static class Builder {
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
