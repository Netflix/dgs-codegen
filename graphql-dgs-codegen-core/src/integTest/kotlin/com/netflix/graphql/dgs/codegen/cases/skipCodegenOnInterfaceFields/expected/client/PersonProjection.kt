package com.netflix.graphql.dgs.codegen.cases.skipCodegenOnInterfaceFields.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.skipCodegenOnInterfaceFields.expected.Generated

@Generated
public class PersonProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val name: PersonProjection
    get() {
      field("name")
      return this
    }
}
