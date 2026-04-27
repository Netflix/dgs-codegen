package com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsWithExtendedQuery.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsWithExtendedQuery.expected.Generated;
import com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsWithExtendedQuery.expected.types.Person;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

@Generated
@DgsComponent
public class PeopleDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "people"
  )
  public List<Person> getPeople(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
