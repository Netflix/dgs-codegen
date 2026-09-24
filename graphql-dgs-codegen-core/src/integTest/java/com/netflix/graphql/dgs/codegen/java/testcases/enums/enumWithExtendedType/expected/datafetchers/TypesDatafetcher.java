package com.netflix.graphql.dgs.codegen.java.testcases.enums.enumWithExtendedType.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.enums.enumWithExtendedType.expected.types.EmployeeTypes;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

@DgsComponent
public class TypesDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "types"
  )
  public List<EmployeeTypes> getTypes(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
