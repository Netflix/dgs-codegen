package com.netflix.graphql.dgs.codegen.cases.interfaceFieldsDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName
import com.netflix.graphql.dgs.codegen.cases.interfaceFieldsDocs.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
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
