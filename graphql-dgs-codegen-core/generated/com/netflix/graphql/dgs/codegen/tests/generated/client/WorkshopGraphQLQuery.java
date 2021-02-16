package com.netflix.graphql.dgs.codegen.tests.generated.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;

public class WorkshopGraphQLQuery extends GraphQLQuery {
  public WorkshopGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "workshop";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    public WorkshopGraphQLQuery build() {
      return new WorkshopGraphQLQuery();
    }
  }
}
