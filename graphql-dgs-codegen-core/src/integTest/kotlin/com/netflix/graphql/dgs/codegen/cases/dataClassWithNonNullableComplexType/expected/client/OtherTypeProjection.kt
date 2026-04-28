package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableComplexType.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableComplexType.expected.Generated

@Generated
public class OtherTypeProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val name: OtherTypeProjection
    get() {
      field("name")
      return this
    }
}
