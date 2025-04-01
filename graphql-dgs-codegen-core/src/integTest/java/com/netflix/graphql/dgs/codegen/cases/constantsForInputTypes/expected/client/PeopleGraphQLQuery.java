package com.netflix.graphql.dgs.codegen.cases.constantsForInputTypes.expected.client;

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;
import com.netflix.graphql.dgs.codegen.cases.constantsForInputTypes.expected.types.PersonFilter;
import java.lang.Override;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class PeopleGraphQLQuery extends GraphQLQuery {
  public PeopleGraphQLQuery(PersonFilter filter, String queryName, Set<String> fieldsSet) {
    super("query", queryName);
    if (filter != null || fieldsSet.contains("filter")) {
        getInput().put("filter", filter);
    }
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

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private PersonFilter filter;

    private String queryName;

    public PeopleGraphQLQuery build() {
      return new PeopleGraphQLQuery(filter, queryName, fieldsSet);
               
    }

    public Builder filter(PersonFilter filter) {
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
