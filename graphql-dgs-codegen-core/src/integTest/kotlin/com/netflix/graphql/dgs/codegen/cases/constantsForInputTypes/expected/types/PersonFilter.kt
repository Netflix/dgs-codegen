package com.netflix.graphql.dgs.codegen.cases.constantsForInputTypes.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public data class PersonFilter @JsonCreator constructor(
  @JsonProperty("email")
  public val email: String? = default<PersonFilter, String?>("email", null),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("email" to email)
}
