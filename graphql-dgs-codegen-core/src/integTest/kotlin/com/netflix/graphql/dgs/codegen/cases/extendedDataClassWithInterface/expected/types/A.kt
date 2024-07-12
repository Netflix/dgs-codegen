package com.netflix.graphql.dgs.codegen.cases.extendedDataClassWithInterface.expected.types

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
  JsonSubTypes.Type(value = Example::class, name = "Example")
])
public sealed interface A {
  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getName")
  public val name: String?
}
