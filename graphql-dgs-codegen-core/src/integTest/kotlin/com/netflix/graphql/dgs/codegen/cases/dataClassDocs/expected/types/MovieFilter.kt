package com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

/**
 * Example filter for Movies.
 *
 * It takes a title and such.
 */
public class MovieFilter @JsonCreator constructor(
  @JsonProperty("titleFilter")
  public val titleFilter: String? = default<MovieFilter, String?>("titleFilter"),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("titleFilter" to titleFilter)
}
