package kotlin2.interfaceClassWithInterfaceFieldsOfDifferentType.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class DietProjection : GraphQLProjection() {
  public val calories: DietProjection
    get() {
      field("calories")
      return this
    }

  public fun onVegetarian(_projection: VegetarianProjection.() -> VegetarianProjection):
      DietProjection {
    project("... on Vegetarian", VegetarianProjection(), _projection)
    return this
  }
}
