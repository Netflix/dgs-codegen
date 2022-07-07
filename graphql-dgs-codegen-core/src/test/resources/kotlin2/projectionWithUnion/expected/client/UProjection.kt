package kotlin2.projectionWithUnion.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class UProjection : GraphQLProjection() {
  public fun onEmployee(_projection: EmployeeProjection.() -> EmployeeProjection): UProjection {
    project("... on Employee", EmployeeProjection(), _projection)
    return this
  }
}
