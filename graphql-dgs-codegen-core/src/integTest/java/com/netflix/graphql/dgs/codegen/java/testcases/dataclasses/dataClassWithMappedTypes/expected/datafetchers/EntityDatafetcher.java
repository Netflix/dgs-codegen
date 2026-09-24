package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedTypes.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedTypes.expected.types.Entity;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

@DgsComponent
public class EntityDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "entity"
  )
  public List<Entity> getEntity(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
