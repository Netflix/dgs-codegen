package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWIthNoFields.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWIthNoFields.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
public class MeGraphQLQuery extends GraphQLQuery {
  public MeGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public MeGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "me";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  @Generated
  public static class Builder {
    private String queryName;

    public MeGraphQLQuery build() {
      return new MeGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
