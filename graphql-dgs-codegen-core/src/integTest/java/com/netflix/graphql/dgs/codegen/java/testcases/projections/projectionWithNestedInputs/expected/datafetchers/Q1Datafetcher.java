package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithNestedInputs.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import graphql.schema.DataFetchingEnvironment;
import java.lang.String;

@DgsComponent
public class Q1Datafetcher {
  @DgsData(
      parentType = "Query",
      field = "q1"
  )
  public String getQ1(DataFetchingEnvironment dataFetchingEnvironment) {
    return "";
  }
}
