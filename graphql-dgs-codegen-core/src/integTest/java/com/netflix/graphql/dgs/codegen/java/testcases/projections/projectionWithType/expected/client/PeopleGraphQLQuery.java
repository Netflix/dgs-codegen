package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithType.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import graphql.language.VariableDefinition;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    private final Map<String, String> variableReferences = new HashMap<>();

    private final List<VariableDefinition> variableDefinitions = new ArrayList<>();

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
