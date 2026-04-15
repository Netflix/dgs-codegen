package com.netflix.graphql.dgs.codegen.cases.inputWithPrefix.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public data class DgsMovieFilter(
  @JsonProperty("genre")
  public val genre: String? = default<DgsMovieFilter, String?>("genre", null),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("genre" to genre)
}
