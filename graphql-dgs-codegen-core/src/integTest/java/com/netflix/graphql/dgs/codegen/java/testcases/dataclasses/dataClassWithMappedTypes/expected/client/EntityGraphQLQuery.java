package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedTypes.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedTypes.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
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

  @Generated
  public static class Builder {
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
