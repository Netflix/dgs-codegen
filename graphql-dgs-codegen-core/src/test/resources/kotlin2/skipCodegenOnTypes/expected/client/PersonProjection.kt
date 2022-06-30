package kotlin2.skipCodegenOnTypes.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class PersonProjection : GraphQLProjection() {
  public val name: PersonProjection
    get() {
      field("name")
      return this
    }
}
