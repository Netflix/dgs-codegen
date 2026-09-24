package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionTypesWithoutInterfaceCanDeserialize.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.unions.unionTypesWithoutInterfaceCanDeserialize.expected.types.SearchResultPage;
import graphql.schema.DataFetchingEnvironment;

@DgsComponent
public class SearchDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "search"
  )
  public SearchResultPage getSearch(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
