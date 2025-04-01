package com.netflix.graphql.dgs.codegen.cases.projectionWithUnion.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.cases.projectionWithUnion.expected.types.U;
import graphql.schema.DataFetchingEnvironment;

@DgsComponent
public class UDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "u"
  )
  public U getU(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
