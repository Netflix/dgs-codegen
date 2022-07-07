package kotlin2.inputWithDefaultStringValueForArray.expected.types

import com.netflix.graphql.dgs.codegen.GraphQLInput
import kotlin.String
import kotlin.collections.List

public class SomeType(
  public val names: List<String?>? = default("names"),
) : GraphQLInput()
