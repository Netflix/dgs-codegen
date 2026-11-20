package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithPrimitives.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithPrimitives.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
public class StringsGraphQLQuery extends GraphQLQuery {
  public StringsGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public StringsGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "strings";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  @Generated
  public static class Builder {
    private String queryName;

    public StringsGraphQLQuery build() {
      return new StringsGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
