package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultIntValueForArray.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.collections.List
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultIntValueForArray.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public data class SomeType(
  @JsonProperty("numbers")
  public val numbers: List<Int?>? = default<SomeType, List<Int?>?>("numbers", listOf(1, 2, 3)),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("numbers" to numbers)
}
