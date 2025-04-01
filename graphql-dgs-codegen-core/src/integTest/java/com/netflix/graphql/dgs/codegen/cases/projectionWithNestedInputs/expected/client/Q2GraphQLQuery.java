package com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.types.I1;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
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

    private I1 arg1;

    private String arg2;

    private String queryName;

    public Q2GraphQLQuery build() {
      return new Q2GraphQLQuery(arg1, arg2, queryName, fieldsSet);
               
    }

    public Builder arg1(I1 arg1) {
      this.arg1 = arg1;
      this.fieldsSet.add("arg1");
      return this;
    }

    public Builder arg2(String arg2) {
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
