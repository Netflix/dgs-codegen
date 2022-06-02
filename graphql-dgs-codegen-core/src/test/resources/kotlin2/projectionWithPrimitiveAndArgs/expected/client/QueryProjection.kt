package kotlin2.projectionWithPrimitiveAndArgs.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection
import kotlin.String
import kotlin2.projectionWithPrimitiveAndArgs.expected.types.I

public class QueryProjection : GraphQLProjection() {
  public fun string(
    a1: String? = default("a1"),
    a2: String,
    a3: I? = default("a3")
  ): QueryProjection {
    val args = formatArgs("a1" to a1, "a2" to a2, "a3" to a3)
    field("string($args)")
    return this
  }
}
