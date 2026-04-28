package com.netflix.graphql.dgs.codegen.cases.dataClassWithInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.netflix.graphql.dgs.codegen.cases.dataClassWithInterfaceInheritance.expected.Generated
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
public sealed interface Person {
  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getFirstname")
  public val firstname: String

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getLastname")
  public val lastname: String?
}
