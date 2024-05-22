package com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class MovieFilter @JsonCreator constructor(
  @JsonProperty("genre")
  public val genre: String? = default<MovieFilter, String?>("genre"),
  @JsonProperty("releaseYear")
  public val releaseYear: Int? = default<MovieFilter, Int?>("releaseYear"),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("genre" to genre, "releaseYear" to
      releaseYear)
}
