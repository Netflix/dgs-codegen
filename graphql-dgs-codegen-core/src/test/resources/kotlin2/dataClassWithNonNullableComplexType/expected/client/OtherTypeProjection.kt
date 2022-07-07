package kotlin2.dataClassWithNonNullableComplexType.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class OtherTypeProjection : GraphQLProjection() {
  public val name: OtherTypeProjection
    get() {
      field("name")
      return this
    }
}
