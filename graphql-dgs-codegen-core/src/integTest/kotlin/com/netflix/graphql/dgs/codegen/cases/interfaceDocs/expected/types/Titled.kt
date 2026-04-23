package com.netflix.graphql.dgs.codegen.cases.interfaceDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName
import com.netflix.graphql.dgs.codegen.cases.interfaceDocs.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

/**
 * Anything with a title!
 */
@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
public sealed interface Titled {
  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getTitle")
  public val title: String?
}
