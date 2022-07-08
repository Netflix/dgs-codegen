package com.netflix.graphql.dgs.codegen.cases.constantsForInputTypes.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.String

public class PersonFilter(
  public val email: String? = default("email"),
) : GraphQLInput()
