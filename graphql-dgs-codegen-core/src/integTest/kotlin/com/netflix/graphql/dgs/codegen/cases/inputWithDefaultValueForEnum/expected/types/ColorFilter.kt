package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForEnum.expected.types

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.collections.List
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForEnum.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
public data class ColorFilter(
  @JsonProperty("color")
  public val color: Color? = default<ColorFilter, Color?>("color",
      com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForEnum.expected.types.Color.red),
) : GraphQLInput() {
  override fun fields(): List<Pair<String, Any?>> = listOf("color" to color)
}
