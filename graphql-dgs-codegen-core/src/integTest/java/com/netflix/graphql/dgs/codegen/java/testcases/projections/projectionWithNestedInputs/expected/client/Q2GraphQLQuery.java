package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithNestedInputs.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithNestedInputs.expected.types.I1;
import graphql.language.VariableDefinition;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Q2GraphQLQuery extends GraphQLQuery {
  public Q2GraphQLQuery(I1 arg1, String arg2, String queryName, Set<String> fieldsSet) {
    super("query", queryName);
    if (arg1 != null || fieldsSet.contains("arg1")) {
        getInput().put("arg1", arg1);
    }if (arg2 != null || fieldsSet.contains("arg2")) {
        getInput().put("arg2", arg2);
    }
  }

  public Q2GraphQLQuery(I1 arg1, String arg2, String queryName, Set<String> fieldsSet,
      Map<String, String> variableReferences, List<VariableDefinition> variableDefinitions) {
    super("query", queryName);
    if (arg1 != null || fieldsSet.contains("arg1")) {
        getInput().put("arg1", arg1);
    }if (arg2 != null || fieldsSet.contains("arg2")) {
        getInput().put("arg2", arg2);
    }
    if(variableDefinitions != null) {
       getVariableDefinitions().addAll(variableDefinitions);
    }

    if(variableReferences != null) {
       getVariableReferences().putAll(variableReferences);
    }                      
  }

  public Q2GraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "q2";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private final Map<String, String> variableReferences = new HashMap<>();

    private final List<VariableDefinition> variableDefinitions = new ArrayList<>();

    private I1 arg1;

    private String arg2;

    private String queryName;

    public Q2GraphQLQuery build() {
      return new Q2GraphQLQuery(arg1, arg2, queryName, fieldsSet, variableReferences, variableDefinitions);
               
    }

    public Builder arg1(I1 arg1) {
      this.arg1 = arg1;
      this.fieldsSet.add("arg1");
      return this;
    }

    public Builder arg1Reference(String variableRef) {
      this.variableReferences.put("arg1", variableRef);
      this.variableDefinitions.add(graphql.language.VariableDefinition.newVariableDefinition(variableRef, new graphql.language.TypeName("I1")).build());
      this.fieldsSet.add("arg1");
      return this;
    }

    public Builder arg2(String arg2) {
      this.arg2 = arg2;
      this.fieldsSet.add("arg2");
      return this;
    }

    public Builder arg2Reference(String variableRef) {
      this.variableReferences.put("arg2", variableRef);
      this.variableDefinitions.add(graphql.language.VariableDefinition.newVariableDefinition(variableRef, new graphql.language.TypeName("String")).build());
      this.fieldsSet.add("arg2");
      return this;
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
