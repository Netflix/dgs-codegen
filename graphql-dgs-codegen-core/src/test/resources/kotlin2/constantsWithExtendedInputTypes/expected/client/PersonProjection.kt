package kotlin2.constantsWithExtendedInputTypes.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class PersonProjection : GraphQLProjection() {
  public val firstname: PersonProjection
    get() {
      field("firstname")
      return this
    }

  public val lastname: PersonProjection
    get() {
      field("lastname")
      return this
    }
}
