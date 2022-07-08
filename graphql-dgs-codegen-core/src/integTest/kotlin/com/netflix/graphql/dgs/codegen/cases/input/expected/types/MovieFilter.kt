package com.netflix.graphql.dgs.codegen.cases.input.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.String

public class MovieFilter(
  public val genre: String? = default("genre"),
) : GraphQLInput()
