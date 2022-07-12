package com.netflix.graphql.dgs.codegen.cases.input.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class MovieFilter(
  public val genre: String? = default<MovieFilter, String?>("genre"),
) : GraphQLInput() {
  public override fun fields(): List<Pair<String, Any?>> = listOf("genre" to genre)
}
