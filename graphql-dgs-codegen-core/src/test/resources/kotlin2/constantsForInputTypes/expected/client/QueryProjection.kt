package kotlin2.constantsForInputTypes.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection
import kotlin2.constantsForInputTypes.expected.types.PersonFilter

public class QueryProjection : GraphQLProjection() {
  public fun people(filter: PersonFilter? = default("filter"),
      _projection: PersonProjection.() -> PersonProjection): QueryProjection {
    val args = formatArgs("filter" to filter)
    project("people($args)", PersonProjection(), _projection)
    return this
  }
}
