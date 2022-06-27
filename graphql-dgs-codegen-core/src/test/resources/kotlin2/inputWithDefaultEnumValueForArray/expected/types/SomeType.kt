package kotlin2.inputWithDefaultEnumValueForArray.expected.types

import com.netflix.graphql.dgs.client.codegen.GraphQLInput
import kotlin.collections.List

public class SomeType(
  public val colors: List<Color?>? = default("colors"),
) : GraphQLInput()
