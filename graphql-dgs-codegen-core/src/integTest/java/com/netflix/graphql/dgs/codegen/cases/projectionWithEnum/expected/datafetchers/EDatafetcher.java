package com.netflix.graphql.dgs.codegen.cases.projectionWithEnum.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.cases.projectionWithEnum.expected.types.E;
import graphql.schema.DataFetchingEnvironment;

@DgsComponent
public class EDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "e"
  )
  public E getE(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
