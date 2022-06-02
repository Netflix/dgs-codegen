package kotlin2.dataClassFieldDocs.expected.types

import com.netflix.graphql.dgs.client.codegen.GraphQLInput
import kotlin.String

public class MovieFilter(
  public val titleFilter: String? = default("titleFilter")
) : GraphQLInput()
