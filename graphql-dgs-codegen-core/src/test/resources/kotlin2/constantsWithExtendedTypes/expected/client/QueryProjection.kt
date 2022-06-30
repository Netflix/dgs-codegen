package kotlin2.constantsWithExtendedTypes.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun people(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    project("people", PersonProjection(), _projection)
    return this
  }
}
