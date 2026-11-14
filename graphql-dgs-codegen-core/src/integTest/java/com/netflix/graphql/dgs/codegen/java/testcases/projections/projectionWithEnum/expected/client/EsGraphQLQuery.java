package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithEnum.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithEnum.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
public class EsGraphQLQuery extends GraphQLQuery {
  public EsGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public EsGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "es";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  @Generated
  public static class Builder {
    private String queryName;

    public EsGraphQLQuery build() {
      return new EsGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
