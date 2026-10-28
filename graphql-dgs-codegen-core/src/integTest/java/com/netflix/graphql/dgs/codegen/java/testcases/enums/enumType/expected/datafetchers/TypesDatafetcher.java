package com.netflix.graphql.dgs.codegen.java.testcases.enums.enumType.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.java.testcases.enums.enumType.expected.Generated;
import com.netflix.graphql.dgs.codegen.java.testcases.enums.enumType.expected.types.EmployeeTypes;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

@Generated
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
