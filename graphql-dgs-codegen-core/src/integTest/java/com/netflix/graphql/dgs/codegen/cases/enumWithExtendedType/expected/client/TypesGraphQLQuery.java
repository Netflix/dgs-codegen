package com.netflix.graphql.dgs.codegen.cases.enumWithExtendedType.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class TypesGraphQLQuery extends GraphQLQuery {
  public TypesGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public TypesGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "types";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private String queryName;

    public TypesGraphQLQuery build() {
      return new TypesGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
