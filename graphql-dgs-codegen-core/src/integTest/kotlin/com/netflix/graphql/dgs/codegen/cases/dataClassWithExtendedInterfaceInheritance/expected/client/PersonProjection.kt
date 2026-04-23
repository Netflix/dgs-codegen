package com.netflix.graphql.dgs.codegen.cases.dataClassWithExtendedInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.dataClassWithExtendedInterfaceInheritance.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class PersonProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val firstname: PersonProjection
    get() {
      field("firstname")
      return this
    }

  public val lastname: PersonProjection
    get() {
      field("lastname")
      return this
    }

  public val age: PersonProjection
    get() {
      field("age")
      return this
    }

  public fun onEmployee(_projection: EmployeeProjection.() -> EmployeeProjection):
      PersonProjection {
    fragment("Employee", EmployeeProjection(), _projection)
    return this
  }
}
