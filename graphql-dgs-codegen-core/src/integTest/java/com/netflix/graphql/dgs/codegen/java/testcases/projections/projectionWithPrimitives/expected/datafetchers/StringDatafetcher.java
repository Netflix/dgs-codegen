package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithPrimitives.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithPrimitives.expected.Generated;
import graphql.schema.DataFetchingEnvironment;
import java.lang.String;

@Generated
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
