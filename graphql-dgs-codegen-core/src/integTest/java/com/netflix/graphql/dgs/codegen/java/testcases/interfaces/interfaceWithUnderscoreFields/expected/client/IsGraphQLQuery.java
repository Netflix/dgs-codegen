package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithUnderscoreFields.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithUnderscoreFields.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
public class IsGraphQLQuery extends GraphQLQuery {
  public IsGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public IsGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "is";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  @Generated
  public static class Builder {
    private String queryName;

    public IsGraphQLQuery build() {
      return new IsGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
