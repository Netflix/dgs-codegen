package com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.Generated

@Generated
public class ActorProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val name: ActorProjection
    get() {
      field("name")
      return this
    }
}
