package kotlin2.dataClassDocs.expected.types

import com.netflix.graphql.dgs.client.codegen.GraphQLInput
import kotlin.String

/**
 * Example filter for Movies.
 *
 * It takes a title and such.
 */
public class MovieFilter(
  public val titleFilter: String? = default("titleFilter")
) : GraphQLInput()
