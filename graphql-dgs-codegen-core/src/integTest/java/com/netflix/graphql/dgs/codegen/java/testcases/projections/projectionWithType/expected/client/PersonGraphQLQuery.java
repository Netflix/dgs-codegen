package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithType.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithType.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
public class PersonGraphQLQuery extends GraphQLQuery {
  public PersonGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public PersonGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "person";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  @Generated
  public static class Builder {
    private String queryName;

    public PersonGraphQLQuery build() {
      return new PersonGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
