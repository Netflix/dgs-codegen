package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected.Generated;
import com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected.types.Fruit;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

@Generated
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
