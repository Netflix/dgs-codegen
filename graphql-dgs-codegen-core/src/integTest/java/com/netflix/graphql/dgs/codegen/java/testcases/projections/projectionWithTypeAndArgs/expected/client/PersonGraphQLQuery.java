package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithTypeAndArgs.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithTypeAndArgs.expected.types.I;
import graphql.language.VariableDefinition;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PersonGraphQLQuery extends GraphQLQuery {
  public PersonGraphQLQuery(String a1, String a2, I a3, String queryName, Set<String> fieldsSet) {
    super("query", queryName);
    if (a1 != null || fieldsSet.contains("a1")) {
        getInput().put("a1", a1);
    }if (a2 != null || fieldsSet.contains("a2")) {
        getInput().put("a2", a2);
    }if (a3 != null || fieldsSet.contains("a3")) {
        getInput().put("a3", a3);
    }
  }

  public PersonGraphQLQuery(String a1, String a2, I a3, String queryName, Set<String> fieldsSet,
      Map<String, String> variableReferences, List<VariableDefinition> variableDefinitions) {
    super("query", queryName);
    if (a1 != null || fieldsSet.contains("a1")) {
        getInput().put("a1", a1);
    }if (a2 != null || fieldsSet.contains("a2")) {
        getInput().put("a2", a2);
    }if (a3 != null || fieldsSet.contains("a3")) {
        getInput().put("a3", a3);
    }
    if(variableDefinitions != null) {
       getVariableDefinitions().addAll(variableDefinitions);
    }

    if(variableReferences != null) {
       getVariableReferences().putAll(variableReferences);
    }                      
  }

  public PersonGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "person";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private final Map<String, String> variableReferences = new HashMap<>();

    private final List<VariableDefinition> variableDefinitions = new ArrayList<>();

    private String a1;

    private String a2;

    private I a3;

    private String queryName;

    public PersonGraphQLQuery build() {
      return new PersonGraphQLQuery(a1, a2, a3, queryName, fieldsSet, variableReferences, variableDefinitions);
               
    }

    public Builder a1(String a1) {
      this.a1 = a1;
      this.fieldsSet.add("a1");
      return this;
    }

    public Builder a1Reference(String variableRef) {
      this.variableReferences.put("a1", variableRef);
      this.variableDefinitions.add(graphql.language.VariableDefinition.newVariableDefinition(variableRef, new graphql.language.TypeName("String")).build());
      this.fieldsSet.add("a1");
      return this;
    }

    public Builder a2(String a2) {
      this.a2 = a2;
      this.fieldsSet.add("a2");
      return this;
    }

    public Builder a2Reference(String variableRef) {
      this.variableReferences.put("a2", variableRef);
      this.variableDefinitions.add(graphql.language.VariableDefinition.newVariableDefinition(variableRef, new graphql.language.NonNullType(new graphql.language.TypeName("String"))).build());
      this.fieldsSet.add("a2");
      return this;
    }

    public Builder a3(I a3) {
      this.a3 = a3;
      this.fieldsSet.add("a3");
      return this;
    }

    public Builder a3Reference(String variableRef) {
      this.variableReferences.put("a3", variableRef);
      this.variableDefinitions.add(graphql.language.VariableDefinition.newVariableDefinition(variableRef, new graphql.language.TypeName("I")).build());
      this.fieldsSet.add("a3");
      return this;
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
