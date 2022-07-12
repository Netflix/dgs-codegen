package com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.types

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
public class MovieFilter(
  public val titleFilter: String? = default<MovieFilter, String?>("titleFilter"),
) : GraphQLInput() {
  public override fun fields(): List<Pair<String, Any?>> = listOf("titleFilter" to titleFilter)
}
