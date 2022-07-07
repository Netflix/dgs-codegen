package kotlin2.dataClassWithDeeplyNestedComplexField.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun cars(_projection: CarProjection.() -> CarProjection): QueryProjection {
    project("cars", CarProjection(), _projection)
    return this
  }
}
