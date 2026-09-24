package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithTypeAndArgs.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithTypeAndArgs.expected.types.Person;
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
