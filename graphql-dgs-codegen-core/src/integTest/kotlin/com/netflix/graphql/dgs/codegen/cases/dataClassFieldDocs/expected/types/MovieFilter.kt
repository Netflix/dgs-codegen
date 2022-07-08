package com.netflix.graphql.dgs.codegen.cases.dataClassFieldDocs.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.String

public class MovieFilter(
  public val titleFilter: String? = default("titleFilter"),
) : GraphQLInput()
