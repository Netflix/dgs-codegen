package com.netflix.graphql.dgs.codegen.java.testcases.misc.typesWithUnderscoreField.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.misc.typesWithUnderscoreField.expected.types.MyInterfaceImpl;
import graphql.schema.DataFetchingEnvironment;

@DgsComponent
public class ImplDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "impl"
  )
  public MyInterfaceImpl getImpl(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
