package kotlin2.unionWithExtendedType.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class MovieProjection : GraphQLProjection() {
  public val title: MovieProjection
    get() {
      field("title")
      return this
    }
}
