package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithExtendedType.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import graphql.schema.DataFetchingEnvironment;
import java.lang.String;
import java.util.List;

@DgsComponent
public class MoviesDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "movies"
  )
  public List<String> getMovies(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
