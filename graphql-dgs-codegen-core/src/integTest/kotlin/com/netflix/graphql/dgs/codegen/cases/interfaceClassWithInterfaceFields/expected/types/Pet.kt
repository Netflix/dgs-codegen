package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.jvm.JvmName

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
@JsonSubTypes(value = [
  JsonSubTypes.Type(value = Dog::class, name = "Dog"),
  JsonSubTypes.Type(value = Bird::class, name = "Bird")
])
public sealed interface Pet {
  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getId")
  public val id: String

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getName")
  public val name: String?

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getAddress")
  public val address: List<String>

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getMother")
  public val mother: Pet

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getFather")
  public val father: Pet?

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getParents")
  public val parents: List<Pet?>?
}
