package com.netflix.graphql.dgs.codegen.cases.input.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public data class MovieFilter @JsonCreator constructor(
  @JsonProperty("genre")
  public val genre: String? = default<MovieFilter, String?>("genre", null),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("genre" to genre)
}
