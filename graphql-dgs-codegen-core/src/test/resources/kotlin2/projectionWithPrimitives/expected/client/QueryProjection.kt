package kotlin2.projectionWithPrimitives.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public val string: QueryProjection
    get() {
      field("string")
      return this
    }

  public val strings: QueryProjection
    get() {
      field("strings")
      return this
    }
}
