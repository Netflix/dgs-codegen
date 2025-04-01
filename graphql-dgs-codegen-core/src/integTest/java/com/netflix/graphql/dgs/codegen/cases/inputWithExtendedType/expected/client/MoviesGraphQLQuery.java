package com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.types.MovieFilter;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class MoviesGraphQLQuery extends GraphQLQuery {
  public MoviesGraphQLQuery(MovieFilter filter, String queryName, Set<String> fieldsSet) {
    super("query", queryName);
    if (filter != null || fieldsSet.contains("filter")) {
        getInput().put("filter", filter);
    }
  }

  public MoviesGraphQLQuery() {
    super("query");
  }

  @Override
  public String getOperationName() {
    return "movies";
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private MovieFilter filter;

    private String queryName;

    public MoviesGraphQLQuery build() {
      return new MoviesGraphQLQuery(filter, queryName, fieldsSet);
               
    }

    public Builder filter(MovieFilter filter) {
      this.filter = filter;
      this.fieldsSet.add("filter");
      return this;
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
