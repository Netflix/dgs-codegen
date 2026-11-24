package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedTypes.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedTypes.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
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

  @Generated
  public static class Builder {
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
