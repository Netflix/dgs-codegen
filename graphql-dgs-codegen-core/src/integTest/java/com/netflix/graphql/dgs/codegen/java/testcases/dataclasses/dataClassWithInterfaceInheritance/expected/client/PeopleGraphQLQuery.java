package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithInterfaceInheritance.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithInterfaceInheritance.expected.Generated;
import java.lang.Override;
import java.lang.String;

@Generated
public class PeopleGraphQLQuery extends GraphQLQuery {
  public PeopleGraphQLQuery(String queryName) {
    super("query", queryName);
  }

  public PeopleGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "people";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  @Generated
  public static class Builder {
    private String queryName;

    public PeopleGraphQLQuery build() {
      return new PeopleGraphQLQuery(queryName);                                     
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
