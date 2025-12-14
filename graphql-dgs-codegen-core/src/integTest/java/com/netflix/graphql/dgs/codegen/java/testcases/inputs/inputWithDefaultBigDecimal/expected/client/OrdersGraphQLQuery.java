package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultBigDecimal.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultBigDecimal.expected.types.OrderFilter;
import graphql.language.VariableDefinition;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrdersGraphQLQuery extends GraphQLQuery {
  public OrdersGraphQLQuery(OrderFilter filter, String queryName, Set<String> fieldsSet) {
    super("query", queryName);
    if (filter != null || fieldsSet.contains("filter")) {
        getInput().put("filter", filter);
    }
  }

  public OrdersGraphQLQuery(OrderFilter filter, String queryName, Set<String> fieldsSet,
      Map<String, String> variableReferences, List<VariableDefinition> variableDefinitions) {
    super("query", queryName);
    if (filter != null || fieldsSet.contains("filter")) {
        getInput().put("filter", filter);
    }
    if(variableDefinitions != null) {
       getVariableDefinitions().addAll(variableDefinitions);
    }

    if(variableReferences != null) {
       getVariableReferences().putAll(variableReferences);
    }                      
  }

  public OrdersGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "orders";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private final Map<String, String> variableReferences = new HashMap<>();

    private final List<VariableDefinition> variableDefinitions = new ArrayList<>();

    private OrderFilter filter;

    private String queryName;

    public OrdersGraphQLQuery build() {
      return new OrdersGraphQLQuery(filter, queryName, fieldsSet, variableReferences, variableDefinitions);
               
    }

    public Builder filter(OrderFilter filter) {
      this.filter = filter;
      this.fieldsSet.add("filter");
      return this;
    }

    public Builder filterReference(String variableRef) {
      this.variableReferences.put("filter", variableRef);
      this.variableDefinitions.add(graphql.language.VariableDefinition.newVariableDefinition(variableRef, new graphql.language.TypeName("OrderFilter")).build());
      this.fieldsSet.add("filter");
      return this;
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
