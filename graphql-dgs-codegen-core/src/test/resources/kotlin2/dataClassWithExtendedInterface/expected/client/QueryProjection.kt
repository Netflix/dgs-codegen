package kotlin2.dataClassWithExtendedInterface.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun people(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    project("people", PersonProjection(), _projection)
    return this
  }
}
