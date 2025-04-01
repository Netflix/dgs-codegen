package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class SearchGraphQLQuery extends GraphQLQuery {
  public SearchGraphQLQuery(String text, String queryName, Set<String> fieldsSet) {
    super("query", queryName);
    if (text != null || fieldsSet.contains("text")) {
        getInput().put("text", text);
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

    private String text;

    private String queryName;

    public SearchGraphQLQuery build() {
      return new SearchGraphQLQuery(text, queryName, fieldsSet);
               
    }

    public Builder text(String text) {
      this.text = text;
      this.fieldsSet.add("text");
      return this;
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
