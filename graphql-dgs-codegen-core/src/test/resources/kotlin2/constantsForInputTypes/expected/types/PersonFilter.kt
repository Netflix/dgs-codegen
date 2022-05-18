package kotlin2.constantsForInputTypes.expected.types

import com.netflix.graphql.dgs.client.codegen.GraphQLInput
import kotlin.String

public class PersonFilter(
  public val email: String? = default("email")
) : GraphQLInput()
