package kotlin2.projectionWithTypeAndArgs.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class EmployeeProjection : GraphQLProjection() {
  public val firstname: EmployeeProjection
    get() {
      field("firstname")
      return this
    }

  public val company: EmployeeProjection
    get() {
      field("company")
      return this
    }
}
