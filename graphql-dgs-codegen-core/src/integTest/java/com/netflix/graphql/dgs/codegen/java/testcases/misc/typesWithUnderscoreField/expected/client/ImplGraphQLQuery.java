package com.netflix.graphql.dgs.codegen.java.testcases.misc.typesWithUnderscoreField.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.misc.typesWithUnderscoreField.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
public class ImplGraphQLQuery extends GraphQLQuery {
  public ImplGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public ImplGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "impl";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  @Generated
  public static class Builder {
    private String queryName;

    public ImplGraphQLQuery build() {
      return new ImplGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
