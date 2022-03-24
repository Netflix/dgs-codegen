package kotlin2.inputWithExtendedType.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection
import kotlin2.inputWithExtendedType.expected.types.MovieFilter

public class QueryProjection : GraphQLProjection() {
  public fun movies(filter: MovieFilter? = default("filter")): QueryProjection {
    val args = formatArgs("filter" to filter)
    field("movies($args)")
    return this
  }
}
