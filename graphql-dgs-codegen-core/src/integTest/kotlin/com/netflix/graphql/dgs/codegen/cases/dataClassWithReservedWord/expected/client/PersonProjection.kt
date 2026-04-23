package com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String
import com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class PersonProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val `interface`: PersonProjection
    get() {
      field("interface")
      return this
    }

  public fun info(`package`: String? = default<PersonProjection, String?>("package")):
      PersonProjection {
    field("info", "package" to `package`)
    return this
  }
}
