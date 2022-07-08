package com.netflix.graphql.dgs.codegen.cases.skipCodegenOnInterfaceFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
public sealed interface Person {
  public val name: String?
}
