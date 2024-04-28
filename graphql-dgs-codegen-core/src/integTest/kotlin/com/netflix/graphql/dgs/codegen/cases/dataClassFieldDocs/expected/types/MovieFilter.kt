package com.netflix.graphql.dgs.codegen.cases.dataClassFieldDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonCreator
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class MovieFilter @JsonCreator constructor(
  @JsonProperty("titleFilter")
  public val titleFilter: String? = default<MovieFilter, String?>("titleFilter", null),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("titleFilter" to titleFilter)
}
