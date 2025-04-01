package com.netflix.graphql.dgs.codegen.cases.dataClassFieldDocs.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.cases.dataClassFieldDocs.expected.types.MovieFilter;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class SearchGraphQLQuery extends GraphQLQuery {
  public SearchGraphQLQuery(MovieFilter movieFilter, String queryName, Set<String> fieldsSet) {
    super("query", queryName);
    if (movieFilter != null || fieldsSet.contains("movieFilter")) {
        getInput().put("movieFilter", movieFilter);
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

    private MovieFilter movieFilter;

    private String queryName;

    public SearchGraphQLQuery build() {
      return new SearchGraphQLQuery(movieFilter, queryName, fieldsSet);
               
    }

    public Builder movieFilter(MovieFilter movieFilter) {
      this.movieFilter = movieFilter;
      this.fieldsSet.add("movieFilter");
      return this;
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}
