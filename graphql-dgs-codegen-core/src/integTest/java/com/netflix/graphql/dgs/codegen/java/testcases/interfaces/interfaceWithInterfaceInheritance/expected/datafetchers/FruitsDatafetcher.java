package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected.types.Fruit;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

@DgsComponent
public class FruitsDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "fruits"
  )
  public List<Fruit> getFruits(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
