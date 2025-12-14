package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeclaredScalars.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeclaredScalars.expected.types.EntityConnection;
import graphql.schema.DataFetchingEnvironment;

@DgsComponent
public class EntityConnectionDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "entityConnection"
  )
  public EntityConnection getEntityConnection(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
