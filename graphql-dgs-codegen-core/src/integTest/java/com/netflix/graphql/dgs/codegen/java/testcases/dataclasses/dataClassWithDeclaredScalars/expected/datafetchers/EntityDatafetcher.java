package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeclaredScalars.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeclaredScalars.expected.types.Entity;
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
