package com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitives.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class StringsGraphQLQuery extends GraphQLQuery {
  public StringsGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public StringsGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "strings";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private String queryName;

    public StringsGraphQLQuery build() {
      return new StringsGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
