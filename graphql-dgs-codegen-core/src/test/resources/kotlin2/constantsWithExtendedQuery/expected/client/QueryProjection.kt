package kotlin2.constantsWithExtendedQuery.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun people(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    project("people", PersonProjection(), _projection)
    return this
  }

  public fun friends(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    project("friends", PersonProjection(), _projection)
    return this
  }
}
