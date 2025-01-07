package com.netflix.graphql.dgs.codegen.cases.dataClassWithPrefix.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import com.netflix.graphql.dgs.codegen.cases.dataClassWithPrefix.expected.Generated
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

/**
 * Example filter for Movies.
 *
 * It takes a title and such.
 */
@Generated
public data class DgsMovieFilter(
  @JsonProperty("titleFilter")
  public val titleFilter: String? = default<DgsMovieFilter, String?>("titleFilter", null),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("titleFilter" to titleFilter)
}
