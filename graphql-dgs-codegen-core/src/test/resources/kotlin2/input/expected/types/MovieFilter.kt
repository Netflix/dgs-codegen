package kotlin2.input.expected.types

import com.netflix.graphql.dgs.client.codegen.GraphQLInput
import kotlin.String

public class MovieFilter(
  public val genre: String? = default("genre"),
) : GraphQLInput()
