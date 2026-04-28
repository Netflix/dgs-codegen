package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.Generated
import kotlin.Suppress
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
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
