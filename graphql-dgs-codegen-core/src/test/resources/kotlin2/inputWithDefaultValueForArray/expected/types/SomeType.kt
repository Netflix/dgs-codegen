package kotlin2.inputWithDefaultValueForArray.expected.types

import com.netflix.graphql.dgs.client.codegen.GraphQLInput
import kotlin.String
import kotlin.collections.List

public class SomeType(
  public val names: List<String?>? = default("names")
) : GraphQLInput()
