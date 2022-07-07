package kotlin2.projectionWithEnum.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public val e: QueryProjection
    get() {
      field("e")
      return this
    }

  public val es: QueryProjection
    get() {
      field("es")
      return this
    }
}
