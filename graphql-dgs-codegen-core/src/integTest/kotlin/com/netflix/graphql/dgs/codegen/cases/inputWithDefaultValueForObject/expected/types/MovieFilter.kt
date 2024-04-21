package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForObject.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List

public class MovieFilter(
  public val director: Person? = default<MovieFilter, Person?>("director"),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("director" to director)
}
