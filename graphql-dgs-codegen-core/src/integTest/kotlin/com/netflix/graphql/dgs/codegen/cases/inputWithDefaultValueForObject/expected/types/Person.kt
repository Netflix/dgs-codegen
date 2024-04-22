package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForObject.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class Person @JsonCreator constructor(
  @JsonProperty("name")
  public val name: String? = default<Person, String?>("name"),
  @JsonProperty("age")
  public val age: Int? = default<Person, Int?>("age"),
  @JsonProperty("car")
  public val car: Car? = default<Person, Car?>("car"),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("name" to name, "age" to age, "car" to
      car)
}
