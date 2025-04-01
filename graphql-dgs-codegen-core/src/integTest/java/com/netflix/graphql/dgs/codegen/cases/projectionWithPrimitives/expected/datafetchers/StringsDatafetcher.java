package com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitives.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import graphql.schema.DataFetchingEnvironment;
import java.lang.String;
import java.util.List;

@DgsComponent
public class StringsDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "strings"
  )
  public List<String> getStrings(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
