package kotlin2.dataClassWithNonNullableAndInterface.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class EmployeeProjection : GraphQLProjection() {
  public val firstname: EmployeeProjection
    get() {
      field("firstname")
      return this
    }

  public val lastname: EmployeeProjection
    get() {
      field("lastname")
      return this
    }

  public val company: EmployeeProjection
    get() {
      field("company")
      return this
    }
}
