package kotlin2.unionTypesWithoutInterfaceCanDeserialize.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class DroidProjection : GraphQLProjection() {
  public val id: DroidProjection
    get() {
      field("id")
      return this
    }

  public val name: DroidProjection
    get() {
      field("name")
      return this
    }

  public val primaryFunction: DroidProjection
    get() {
      field("primaryFunction")
      return this
    }
}
