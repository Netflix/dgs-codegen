package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeeplyNestedComplexField.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeeplyNestedComplexField.expected.types.Car;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

@DgsComponent
public class CarsDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "cars"
  )
  public List<Car> getCars(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
