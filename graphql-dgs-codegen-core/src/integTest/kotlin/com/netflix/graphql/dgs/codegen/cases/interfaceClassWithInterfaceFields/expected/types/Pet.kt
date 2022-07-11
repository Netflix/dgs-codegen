package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String
import kotlin.collections.List

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
  public val id: String

  public val name: String?

  public val address: List<String>

  public val mother: Pet

  public val father: Pet?

  public val parents: List<Pet?>?
}
