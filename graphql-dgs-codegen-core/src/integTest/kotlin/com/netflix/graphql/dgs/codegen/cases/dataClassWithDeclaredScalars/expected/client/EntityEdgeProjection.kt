package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.client

import com.netflix.graphql.dgs.client.codegen.InputValueSerializerInterface
import com.netflix.graphql.dgs.codegen.GraphQLProjection
import kotlin.String
import com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public class EntityEdgeProjection(
  inputValueSerializer: InputValueSerializerInterface? = null,
) : GraphQLProjection(inputValueSerializer) {
  public val cursor: EntityEdgeProjection
    get() {
      field("cursor")
      return this
    }

  public fun node(_alias: String? = null, _projection: EntityProjection.() -> EntityProjection):
      EntityEdgeProjection {
    field(_alias, "node", EntityProjection(inputValueSerializer), _projection)
    return this
  }
}
