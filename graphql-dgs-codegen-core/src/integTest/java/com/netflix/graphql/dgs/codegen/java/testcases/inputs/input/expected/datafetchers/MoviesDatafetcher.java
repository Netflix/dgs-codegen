package com.netflix.graphql.dgs.codegen.java.testcases.inputs.input.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.inputs.input.expected.Generated;
import graphql.schema.DataFetchingEnvironment;
import java.lang.String;
import java.util.List;

@Generated
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
