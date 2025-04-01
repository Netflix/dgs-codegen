package com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.types.I2;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class Q1GraphQLQuery extends GraphQLQuery {
  public Q1GraphQLQuery(String arg1, I2 arg2, String queryName, Set<String> fieldsSet) {
    super("query", queryName);
    if (arg1 != null || fieldsSet.contains("arg1")) {
        getInput().put("arg1", arg1);
    }if (arg2 != null || fieldsSet.contains("arg2")) {
        getInput().put("arg2", arg2);
    }
  }

  public Q1GraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "q1";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private String arg1;

    private I2 arg2;

    private String queryName;

    public Q1GraphQLQuery build() {
      return new Q1GraphQLQuery(arg1, arg2, queryName, fieldsSet);
               
    }

    public Builder arg1(String arg1) {
      this.arg1 = arg1;
      this.fieldsSet.add("arg1");
      return this;
    }

    public Builder arg2(I2 arg2) {
      this.arg2 = arg2;
      this.fieldsSet.add("arg2");
      return this;
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
