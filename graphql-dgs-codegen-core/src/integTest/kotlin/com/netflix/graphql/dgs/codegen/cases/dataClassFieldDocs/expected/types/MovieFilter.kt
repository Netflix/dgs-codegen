package com.netflix.graphql.dgs.codegen.cases.dataClassFieldDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List
import com.netflix.graphql.dgs.codegen.cases.dataClassFieldDocs.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public data class MovieFilter(
  @JsonProperty("titleFilter")
  public val titleFilter: String? = default<MovieFilter, String?>("titleFilter", null),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("titleFilter" to titleFilter)
}
