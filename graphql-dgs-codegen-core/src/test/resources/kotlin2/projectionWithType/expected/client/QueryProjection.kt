package kotlin2.projectionWithType.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun person(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    project("person", PersonProjection(), _projection)
    return this
  }

  public fun people(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    project("people", PersonProjection(), _projection)
    return this
  }
}
