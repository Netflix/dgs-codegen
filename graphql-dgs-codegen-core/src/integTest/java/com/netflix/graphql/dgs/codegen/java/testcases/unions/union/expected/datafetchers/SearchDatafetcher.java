package com.netflix.graphql.dgs.codegen.java.testcases.unions.union.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.unions.union.expected.types.SearchResult;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

@DgsComponent
public class SearchDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "search"
  )
  public List<SearchResult> getSearch(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
