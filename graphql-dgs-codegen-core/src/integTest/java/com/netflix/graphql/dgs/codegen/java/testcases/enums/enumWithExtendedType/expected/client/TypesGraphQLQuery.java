package com.netflix.graphql.dgs.codegen.java.testcases.enums.enumWithExtendedType.expected.client;

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

    private final Map<String, String> variableReferences = new HashMap<>();

    private final List<VariableDefinition> variableDefinitions = new ArrayList<>();

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
