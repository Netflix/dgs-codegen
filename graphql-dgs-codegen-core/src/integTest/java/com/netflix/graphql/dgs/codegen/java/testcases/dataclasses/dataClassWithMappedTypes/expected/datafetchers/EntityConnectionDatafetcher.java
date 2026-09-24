package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedTypes.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedTypes.expected.types.EntityEdge;
import graphql.relay.SimpleListConnection;
import graphql.schema.DataFetchingEnvironment;

@DgsComponent
public class EntityConnectionDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "entityConnection"
  )
  public SimpleListConnection<EntityEdge> getEntityConnection(
      DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
