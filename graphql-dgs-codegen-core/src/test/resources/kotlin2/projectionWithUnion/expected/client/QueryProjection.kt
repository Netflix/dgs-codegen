package kotlin2.projectionWithUnion.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun u(_projection: UProjection.() -> UProjection): QueryProjection {
    project("u", UProjection(), _projection)
    return this
  }

  public fun us(_projection: UProjection.() -> UProjection): QueryProjection {
    project("us", UProjection(), _projection)
    return this
  }
}
