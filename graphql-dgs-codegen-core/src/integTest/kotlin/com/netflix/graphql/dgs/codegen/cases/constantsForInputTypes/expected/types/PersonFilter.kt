package com.netflix.graphql.dgs.codegen.cases.constantsForInputTypes.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import com.netflix.graphql.dgs.codegen.cases.constantsForInputTypes.expected.Generated
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

@Generated
public data class PersonFilter(
  @JsonProperty("email")
  public val email: String? = default<PersonFilter, String?>("email", null),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("email" to email)
}
