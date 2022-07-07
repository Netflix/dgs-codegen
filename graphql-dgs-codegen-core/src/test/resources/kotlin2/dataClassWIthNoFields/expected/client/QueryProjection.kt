package kotlin2.dataClassWIthNoFields.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun me(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    project("me", PersonProjection(), _projection)
    return this
  }
}
