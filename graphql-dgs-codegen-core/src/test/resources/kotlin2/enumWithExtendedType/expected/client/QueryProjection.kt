package kotlin2.enumWithExtendedType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public val types: QueryProjection
    get() {
      field("types")
      return this
    }
}
