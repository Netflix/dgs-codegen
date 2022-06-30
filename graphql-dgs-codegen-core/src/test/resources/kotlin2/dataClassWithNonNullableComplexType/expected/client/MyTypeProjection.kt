package kotlin2.dataClassWithNonNullableComplexType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class MyTypeProjection : GraphQLProjection() {
  public fun other(_projection: OtherTypeProjection.() -> OtherTypeProjection): MyTypeProjection {
    project("other", OtherTypeProjection(), _projection)
    return this
  }
}
