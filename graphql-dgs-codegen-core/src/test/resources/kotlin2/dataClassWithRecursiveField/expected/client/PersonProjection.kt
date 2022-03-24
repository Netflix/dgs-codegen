package kotlin2.dataClassWithRecursiveField.expected.client

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

  public fun friends(_projection: PersonProjection.() -> PersonProjection): PersonProjection {
    project("friends", PersonProjection(), _projection)
    return this
  }
}
