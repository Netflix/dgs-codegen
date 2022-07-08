package com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.Int
import kotlin.String

public class MovieFilter(
  public val genre: String? = default("genre"),
  public val releaseYear: Int? = default("releaseYear"),
) : GraphQLInput()
