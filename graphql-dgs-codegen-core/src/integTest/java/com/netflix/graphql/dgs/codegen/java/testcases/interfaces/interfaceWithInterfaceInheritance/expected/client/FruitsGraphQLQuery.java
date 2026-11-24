package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
public class FruitsGraphQLQuery extends GraphQLQuery {
  public FruitsGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public FruitsGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "fruits";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  @Generated
  public static class Builder {
    private String queryName;

    public FruitsGraphQLQuery build() {
      return new FruitsGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
