package kotlin2.constantsWithExtendedInputTypes.expected.types

import com.netflix.graphql.dgs.client.codegen.GraphQLInput
import kotlin.Int
import kotlin.String

public class PersonFilter(
  public val email: String? = default("email"),
  public val birthYear: Int? = default("birthYear"),
) : GraphQLInput()
