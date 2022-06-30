package kotlin2.unionTypesWithoutInterfaceCanDeserialize.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class HumanProjection : GraphQLProjection() {
  public val id: HumanProjection
    get() {
      field("id")
      return this
    }

  public val name: HumanProjection
    get() {
      field("name")
      return this
    }

  public val totalCredits: HumanProjection
    get() {
      field("totalCredits")
      return this
    }
}
