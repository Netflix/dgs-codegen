package com.netflix.graphql.dgs.codegen.cases.dataClassWithListProperties.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class PeopleGraphQLQuery extends GraphQLQuery {
  public PeopleGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public PeopleGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "people";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private String queryName;

    public PeopleGraphQLQuery build() {
      return new PeopleGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
