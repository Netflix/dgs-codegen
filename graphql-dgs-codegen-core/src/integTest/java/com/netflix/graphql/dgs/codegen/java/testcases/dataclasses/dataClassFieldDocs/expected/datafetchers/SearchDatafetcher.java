package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassFieldDocs.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassFieldDocs.expected.Generated;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassFieldDocs.expected.types.Movie;
import graphql.schema.DataFetchingEnvironment;

@Generated
@DgsComponent
public class SearchDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "search"
  )
  public Movie getSearch(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
