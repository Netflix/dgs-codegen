package com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.types.Person;
import graphql.schema.DataFetchingEnvironment;

@DgsComponent
public class MeDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "me"
  )
  public Person getMe(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
