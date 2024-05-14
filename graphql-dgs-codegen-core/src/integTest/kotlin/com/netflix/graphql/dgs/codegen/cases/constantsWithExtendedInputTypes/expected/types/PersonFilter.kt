package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedInputTypes.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class PersonFilter @JsonCreator constructor(
  @JsonProperty("email")
  public val email: String? = default<PersonFilter, String?>("email"),
  @JsonProperty("birthYear")
  public val birthYear: Int? = default<PersonFilter, Int?>("birthYear"),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("email" to email, "birthYear" to
      birthYear)
}
