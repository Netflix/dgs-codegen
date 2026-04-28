package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.types

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.Generated
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
@JsonSubTypes(value = [
  JsonSubTypes.Type(value = Vegetarian::class, name = "Vegetarian")
])
public sealed interface Diet {
  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getCalories")
  public val calories: String?
}
