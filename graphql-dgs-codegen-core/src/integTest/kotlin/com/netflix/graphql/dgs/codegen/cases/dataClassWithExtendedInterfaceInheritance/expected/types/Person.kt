package com.netflix.graphql.dgs.codegen.cases.dataClassWithExtendedInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
@JsonSubTypes(value = [
  JsonSubTypes.Type(value = Employee::class, name = "Employee")
])
public sealed interface Person {
  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getFirstname")
  public val firstname: String

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getLastname")
  public val lastname: String?

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getAge")
  public val age: Int
}
