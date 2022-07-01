package kotlin2.dataClassWithMappedTypes.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class EntityProjection : GraphQLProjection() {
  public val long: EntityProjection
    get() {
      field("long")
      return this
    }

  public val dateTime: EntityProjection
    get() {
      field("dateTime")
      return this
    }
}
