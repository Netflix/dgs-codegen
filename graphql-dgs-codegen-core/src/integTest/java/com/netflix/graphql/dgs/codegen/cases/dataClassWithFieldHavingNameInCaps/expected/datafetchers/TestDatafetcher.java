package com.netflix.graphql.dgs.codegen.cases.dataClassWithFieldHavingNameInCaps.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.cases.dataClassWithFieldHavingNameInCaps.expected.types.SomeType;
import graphql.schema.DataFetchingEnvironment;

@DgsComponent
public class TestDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "test"
  )
  public SomeType getTest(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
