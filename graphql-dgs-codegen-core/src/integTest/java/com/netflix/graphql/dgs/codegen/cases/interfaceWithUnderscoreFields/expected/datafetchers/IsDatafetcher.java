package com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.types.I;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

@DgsComponent
public class IsDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "is"
  )
  public List<I> getIs(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
