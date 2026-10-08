package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultMap.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultMap.expected.Generated
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

@Generated
public data class MovieFilter(
  @JsonProperty("metadata")
  public val metadata: Map<String, Any?> = default<MovieFilter, Map<String, Any?>>("metadata",
      emptyMap()),
  @JsonProperty("details")
  public val details: Map<String, Any?>? = default<MovieFilter, Map<String, Any?>?>("details",
      mapOf("title" to "Alien", "year" to 1_979, "tags" to listOf("sci-fi"), "director" to
      mapOf("name" to "Ridley Scott"))),
  @JsonProperty("variants")
  public val variants: List<Map<String, Any?>?>? = default<MovieFilter,
      List<Map<String, Any?>?>?>("variants", listOf(emptyMap(), mapOf("title" to "Aliens"))),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("metadata" to metadata, "details" to
      details, "variants" to variants)
}
