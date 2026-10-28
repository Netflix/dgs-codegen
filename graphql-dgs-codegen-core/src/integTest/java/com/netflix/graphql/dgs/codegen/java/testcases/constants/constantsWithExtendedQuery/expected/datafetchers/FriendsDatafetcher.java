package com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsWithExtendedQuery.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsWithExtendedQuery.expected.Generated;
import com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsWithExtendedQuery.expected.types.Person;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

@Generated
@DgsComponent
public class FriendsDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "friends"
  )
  public List<Person> getFriends(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
