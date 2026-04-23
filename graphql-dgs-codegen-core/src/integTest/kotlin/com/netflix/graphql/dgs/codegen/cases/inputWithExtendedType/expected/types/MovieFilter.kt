package com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.collections.List
import com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public data class MovieFilter(
  @JsonProperty("genre")
  public val genre: String? = default<MovieFilter, String?>("genre", null),
  @JsonProperty("releaseYear")
  public val releaseYear: Int? = default<MovieFilter, Int?>("releaseYear", null),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("genre" to genre, "releaseYear" to
      releaseYear)
}
