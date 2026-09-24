package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithType.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithType.expected.types.Person;
import graphql.schema.DataFetchingEnvironment;

@DgsComponent
public class PersonDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "person"
  )
  public Person getPerson(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
