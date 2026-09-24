package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithNestedInputs.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import graphql.schema.DataFetchingEnvironment;
import java.lang.String;

@DgsComponent
public class Q2Datafetcher {
  @DgsData(
      parentType = "Query",
      field = "q2"
  )
  public String getQ2(DataFetchingEnvironment dataFetchingEnvironment) {
    return "";
  }
}
