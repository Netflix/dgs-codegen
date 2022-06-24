package kotlin2.projectionWithSameDefinitions.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class ShowProjection : GraphQLProjection() {
  public val id: ShowProjection
    get() {
      field("id")
      return this
    }

  public val title: ShowProjection
    get() {
      field("title")
      return this
    }
}
