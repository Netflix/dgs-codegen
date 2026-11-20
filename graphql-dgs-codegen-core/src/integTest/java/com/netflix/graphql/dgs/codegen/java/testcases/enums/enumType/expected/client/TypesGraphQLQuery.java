package com.netflix.graphql.dgs.codegen.java.testcases.enums.enumType.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.enums.enumType.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
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

  @Generated
  public static class Builder {
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
