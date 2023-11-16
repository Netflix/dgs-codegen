package com.netflix.graphql.dgs.codegen.cases.dataClassWithInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
@JsonSubTypes(value = [
  JsonSubTypes.Type(value = Talent::class, name = "Talent")
])
public sealed interface Employee : Person {
  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getFirstname")
  override val firstname: String

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getLastname")
  override val lastname: String?

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getCompany")
  public val company: String?
}
