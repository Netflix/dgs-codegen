package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.Suppress
import kotlin.collections.List
import kotlin.jvm.JvmName
import com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
public sealed interface Fruit {
  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getSeeds")
  public val seeds: List<Seed?>?
}
