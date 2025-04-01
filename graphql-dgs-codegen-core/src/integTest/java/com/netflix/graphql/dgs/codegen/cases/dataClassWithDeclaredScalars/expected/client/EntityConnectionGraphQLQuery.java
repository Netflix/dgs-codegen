package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class EntityConnectionGraphQLQuery extends GraphQLQuery {
  public EntityConnectionGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public EntityConnectionGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "entityConnection";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private String queryName;

    public EntityConnectionGraphQLQuery build() {
      return new EntityConnectionGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
