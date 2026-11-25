package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForNonNullableFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForNonNullableFields.expected.Generated
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

@Generated
public data class Person(
  @param:JsonProperty("name")
  @field:JsonProperty("name")
  public val name: String = default<Person, String>("name", "Damian"),
  @param:JsonProperty("age")
  @field:JsonProperty("age")
  public val age: Int = default<Person, Int>("age", 18),
  @param:JsonProperty("car")
  @field:JsonProperty("car")
  public val car:
      Car = default<Person, Car>("car", com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForNonNullableFields.expected.types.Car(brand = "Ford")),
  @param:JsonProperty("hobbies")
  @field:JsonProperty("hobbies")
  public val hobbies:
      List<Hobby> = default<Person, List<Hobby>>("hobbies", listOf(com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForNonNullableFields.expected.types.Hobby.Hokey)),
  @param:JsonProperty("isHappy")
  @field:JsonProperty("isHappy")
  public val isHappy: Boolean = default<Person, Boolean>("isHappy", true),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("name" to name, "age" to age, "car" to car, "hobbies" to hobbies, "isHappy" to isHappy)
}
