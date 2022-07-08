package com.netflix.graphql.dgs.codegen.cases.interfaceDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

/**
 * Anything with a title!
 */
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
public sealed interface Titled {
  public val title: String?
}
