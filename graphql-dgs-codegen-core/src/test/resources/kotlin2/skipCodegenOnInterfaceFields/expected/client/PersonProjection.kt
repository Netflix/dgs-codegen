package kotlin2.skipCodegenOnInterfaceFields.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class PersonProjection : GraphQLProjection() {
  public val name: PersonProjection
    get() {
      field("name")
      return this
    }
}
