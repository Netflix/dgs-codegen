package kotlin2.projectionWithTypeAndArgs.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection
import kotlin.String
import kotlin2.projectionWithTypeAndArgs.expected.types.I

public class QueryProjection : GraphQLProjection() {
  public fun person(
    a1: String? = default("a1"),
    a2: String,
    a3: I? = default("a3"),
    _projection: PersonProjection.() -> PersonProjection
  ): QueryProjection {
    val args = formatArgs("a1" to a1, "a2" to a2, "a3" to a3)
    project("person($args)", PersonProjection(), _projection)
    return this
  }
}
