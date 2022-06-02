package kotlin2.interfaceWithInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class QueryProjection : GraphQLProjection() {
  public fun fruits(_projection: FruitProjection.() -> FruitProjection): QueryProjection {
    project("fruits", FruitProjection(), _projection)
    return this
  }
}
