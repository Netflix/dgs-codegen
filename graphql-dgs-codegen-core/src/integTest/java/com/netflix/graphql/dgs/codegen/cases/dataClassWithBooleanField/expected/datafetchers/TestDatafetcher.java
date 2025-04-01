package com.netflix.graphql.dgs.codegen.cases.dataClassWithBooleanField.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.cases.dataClassWithBooleanField.expected.types.RequiredTestType;
import graphql.schema.DataFetchingEnvironment;

@DgsComponent
public class TestDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "test"
  )
  public RequiredTestType getTest(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
