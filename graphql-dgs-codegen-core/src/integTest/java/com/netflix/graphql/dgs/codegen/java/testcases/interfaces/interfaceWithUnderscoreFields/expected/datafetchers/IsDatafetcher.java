package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithUnderscoreFields.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithUnderscoreFields.expected.types.I;
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
