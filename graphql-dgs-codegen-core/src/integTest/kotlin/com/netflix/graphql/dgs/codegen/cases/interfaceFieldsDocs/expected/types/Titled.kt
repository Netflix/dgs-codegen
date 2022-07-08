package com.netflix.graphql.dgs.codegen.cases.interfaceFieldsDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
public sealed interface Titled {
  /**
   * The original, non localized title.
   */
  public val title: String?
}
