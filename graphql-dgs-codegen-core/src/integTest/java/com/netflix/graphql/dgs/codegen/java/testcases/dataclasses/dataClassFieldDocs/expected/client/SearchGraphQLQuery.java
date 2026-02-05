package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassFieldDocs.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassFieldDocs.expected.types.MovieFilter;
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
  public SearchGraphQLQuery(MovieFilter movieFilter, String queryName, Set<String> fieldsSet) {
    super("query", queryName);
    if (movieFilter != null || fieldsSet.contains("movieFilter")) {
        getInput().put("movieFilter", movieFilter);
    }
  }

  public SearchGraphQLQuery(MovieFilter movieFilter, String queryName, Set<String> fieldsSet,
      Map<String, String> variableReferences, List<VariableDefinition> variableDefinitions) {
    super("query", queryName);
    if (movieFilter != null || fieldsSet.contains("movieFilter")) {
        getInput().put("movieFilter", movieFilter);
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

    private MovieFilter movieFilter;

    private String queryName;

    public SearchGraphQLQuery build() {
      return new SearchGraphQLQuery(movieFilter, queryName, fieldsSet, variableReferences, variableDefinitions);
               
    }

    public Builder movieFilter(MovieFilter movieFilter) {
      this.movieFilter = movieFilter;
      this.fieldsSet.add("movieFilter");
      return this;
    }

    public Builder movieFilterReference(String variableRef) {
      this.variableReferences.put("movieFilter", variableRef);
      this.variableDefinitions.add(graphql.language.VariableDefinition.newVariableDefinition(variableRef, new graphql.language.NonNullType(new graphql.language.TypeName("MovieFilter"))).build());
      this.fieldsSet.add("movieFilter");
      return this;
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
