package com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

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

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

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
