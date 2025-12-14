package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithUnion.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithUnion.expected.types.U;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

@DgsComponent
public class UsDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "us"
  )
  public List<U> getUs(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
