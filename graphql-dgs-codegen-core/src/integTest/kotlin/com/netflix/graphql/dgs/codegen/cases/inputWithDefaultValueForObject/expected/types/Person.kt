package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForObject.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public data class Person @JsonCreator constructor(
  @JsonProperty("name")
  public val name: String? = default<Person, String?>("name", "John"),
  @JsonProperty("age")
  public val age: Int? = default<Person, Int?>("age", 23),
  @JsonProperty("car")
  public val car: Car? = default<Person, Car?>("car",
      com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForObject.expected.types.Car(brand
      = "Ford")),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("name" to name, "age" to age, "car" to
      car)
}
