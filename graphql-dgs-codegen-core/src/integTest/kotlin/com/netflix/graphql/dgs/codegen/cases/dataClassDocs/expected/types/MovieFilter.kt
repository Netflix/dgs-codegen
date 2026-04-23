package com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List
import com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

/**
 * Example filter for Movies.
 *
 * It takes a title and such.
 */
@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public data class MovieFilter(
  @JsonProperty("titleFilter")
  public val titleFilter: String? = default<MovieFilter, String?>("titleFilter", null),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("titleFilter" to titleFilter)
}
