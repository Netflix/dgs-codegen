package com.netflix.graphql.dgs.codegen.cases.projectionWithType.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

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

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

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
