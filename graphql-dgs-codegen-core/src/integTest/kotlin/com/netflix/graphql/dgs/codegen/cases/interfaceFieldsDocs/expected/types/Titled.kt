package com.netflix.graphql.dgs.codegen.cases.interfaceFieldsDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.netflix.graphql.dgs.codegen.cases.interfaceFieldsDocs.expected.Generated
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
public sealed interface Titled {
  /**
   * The original, non localized title.
   */
  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getTitle")
  public val title: String?
}
