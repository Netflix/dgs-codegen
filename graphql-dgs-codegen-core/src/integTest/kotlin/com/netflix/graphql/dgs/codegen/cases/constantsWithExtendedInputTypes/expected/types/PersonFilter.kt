package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedInputTypes.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedInputTypes.expected.Generated
import kotlin.Any
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

@Generated
public data class PersonFilter(
  @param:JsonProperty("email")
  @field:JsonProperty("email")
  public val email: String? = default<PersonFilter, String?>("email", null),
  @param:JsonProperty("birthYear")
  @field:JsonProperty("birthYear")
  public val birthYear: Int? = default<PersonFilter, Int?>("birthYear", null),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("email" to email, "birthYear" to birthYear)
}
