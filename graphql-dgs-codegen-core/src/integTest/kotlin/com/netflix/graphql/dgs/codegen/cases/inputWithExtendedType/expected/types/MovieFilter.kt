package com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.Generated
import kotlin.Any
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

@Generated
public data class MovieFilter(
  @param:JsonProperty("genre")
  @field:JsonProperty("genre")
  public val genre: String? = default<MovieFilter, String?>("genre", null),
  @param:JsonProperty("releaseYear")
  @field:JsonProperty("releaseYear")
  public val releaseYear: Int? = default<MovieFilter, Int?>("releaseYear", null),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("genre" to genre, "releaseYear" to releaseYear)
}
