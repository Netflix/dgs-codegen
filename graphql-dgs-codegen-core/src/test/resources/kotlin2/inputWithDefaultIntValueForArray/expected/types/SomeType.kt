package kotlin2.inputWithDefaultIntValueForArray.expected.types

import com.netflix.graphql.dgs.client.codegen.GraphQLInput
import kotlin.Int
import kotlin.collections.List

public class SomeType(
  public val numbers: List<Int?>? = default("numbers")
) : GraphQLInput()
