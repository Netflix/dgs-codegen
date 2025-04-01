package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedQuery.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedQuery.expected.types.Person;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

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
