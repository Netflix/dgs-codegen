package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithBooleanField.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithBooleanField.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
public class TestGraphQLQuery extends GraphQLQuery {
  public TestGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public TestGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "test";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  @Generated
  public static class Builder {
    private String queryName;

    public TestGraphQLQuery build() {
      return new TestGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
