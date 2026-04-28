package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.Generated
import kotlin.Boolean
import kotlin.Suppress
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
public sealed interface StoneFruit : Fruit {
  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getSeeds")
  override val seeds: List<Seed?>?

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getFuzzy")
  public val fuzzy: Boolean?
}
