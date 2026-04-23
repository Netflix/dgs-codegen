package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForObject.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForObject.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public data class MovieFilter(
  @JsonProperty("director")
  public val director: Person? = default<MovieFilter, Person?>("director",
      com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForObject.expected.types.Person(name
      = "Damian", car =
      com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForObject.expected.types.Car(brand
      = "Tesla"))),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("director" to director)
}
