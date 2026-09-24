package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithEnum.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithEnum.expected.types.E;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

@DgsComponent
public class EsDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "es"
  )
  public List<E> getEs(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
