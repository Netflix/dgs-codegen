package com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitiveAndArgs.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import graphql.schema.DataFetchingEnvironment;
import java.lang.String;

@DgsComponent
public class StringDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "string"
  )
  public String getString(DataFetchingEnvironment dataFetchingEnvironment) {
    return "";
  }
}
