package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class EntityGraphQLQuery extends GraphQLQuery {
  public EntityGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public EntityGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "entity";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private String queryName;

    public EntityGraphQLQuery build() {
      return new EntityGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
