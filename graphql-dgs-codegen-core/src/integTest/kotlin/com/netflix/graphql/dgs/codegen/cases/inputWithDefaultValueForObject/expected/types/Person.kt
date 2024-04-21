package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForObject.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class Person(
  public val name: String? = default<Person, String?>("name"),
  public val age: Int? = default<Person, Int?>("age"),
  public val car: Car? = default<Person, Car?>("car"),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("name" to name, "age" to age, "car" to
      car)
}
