package com.netflix.graphql.dgs.codegen.cases.dataClassFieldDocs.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class MovieFilter(
  public val titleFilter: String? = default<MovieFilter, String?>("titleFilter"),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("titleFilter" to titleFilter)
}
