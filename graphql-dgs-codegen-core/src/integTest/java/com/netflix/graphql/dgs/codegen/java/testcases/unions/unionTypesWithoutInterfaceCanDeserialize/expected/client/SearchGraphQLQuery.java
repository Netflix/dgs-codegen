package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionTypesWithoutInterfaceCanDeserialize.expected.client;

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

public class SearchGraphQLQuery extends GraphQLQuery {
  public SearchGraphQLQuery(String text, String queryName, Set<String> fieldsSet) {
    super("query", queryName);
    if (text != null || fieldsSet.contains("text")) {
        getInput().put("text", text);
    }
  }

  public SearchGraphQLQuery(String text, String queryName, Set<String> fieldsSet,
      Map<String, String> variableReferences, List<VariableDefinition> variableDefinitions) {
    super("query", queryName);
    if (text != null || fieldsSet.contains("text")) {
        getInput().put("text", text);
    }
    if(variableDefinitions != null) {
       getVariableDefinitions().addAll(variableDefinitions);
    }

    if(variableReferences != null) {
       getVariableReferences().putAll(variableReferences);
    }                      
  }

  public SearchGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "search";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private final Map<String, String> variableReferences = new HashMap<>();

    private final List<VariableDefinition> variableDefinitions = new ArrayList<>();

    private String text;

    private String queryName;

    public SearchGraphQLQuery build() {
      return new SearchGraphQLQuery(text, queryName, fieldsSet, variableReferences, variableDefinitions);
               
    }

    public Builder text(String text) {
      this.text = text;
      this.fieldsSet.add("text");
      return this;
    }

    public Builder textReference(String variableRef) {
      this.variableReferences.put("text", variableRef);
      this.variableDefinitions.add(graphql.language.VariableDefinition.newVariableDefinition(variableRef, new graphql.language.NonNullType(new graphql.language.TypeName("String"))).build());
      this.fieldsSet.add("text");
      return this;
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
