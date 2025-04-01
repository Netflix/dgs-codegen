package com.netflix.graphql.dgs.codegen.cases.projectionWithEnum.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

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

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

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
